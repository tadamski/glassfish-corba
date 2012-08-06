/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * 
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 * 
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.corba.ee.impl.protocol;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.protocol.MessageMediator;
import com.sun.corba.ee.spi.protocol.RequestId;
import com.sun.corba.ee.spi.transport.Connection;
import com.sun.corba.ee.spi.protocol.MessageParser;

import com.sun.corba.ee.impl.protocol.giopmsgheaders.Message;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.MessageBase;
import com.sun.corba.ee.spi.trace.Giop;
import com.sun.corba.ee.spi.trace.Transport;


/**
 *
 * An implementation of a <code>MessageParser</code> that knows how to parse
 * bytes into a GIOP protocol data unit.
 *
 *
 */
@Transport
@Giop
public class MessageParserImpl implements MessageParser {

    private static final int NUM_BYTES_IN_INTEGER = 4;
    private final static int MESSAGE_LENGTH_INDEX = Message.GIOPMessageHeaderLength - NUM_BYTES_IN_INTEGER;
    final private ORB orb;
    private boolean expectingMoreData;
    private boolean moreBytesToParse;
    private int nextMsgStartPos;
    private int sizeNeeded;
    /**
     * A list of request ids awaiting final fragments.  When the size of
     * this list is larger than 0, we have received a fragmented message
     * and expecting to receive more message fragments for that given
     * request id on this list.  Hence, if there are entries in this list
     * we are expecting more data to arrive.
     * We are using a List here rather than a Set since the size of the
     * List is expected to be rather small, (i.e. less than size 10).
     */
    private List<RequestId> fragmentList;
    private ByteBuffer msgByteBuffer;

    /** The buffer which will be returned for additional input. */
    private ByteBuffer remainderBuffer;

    /** wrapped message create by the last call to offerBuffer. **/
    private MessageMediator messageMediator;
    private Connection connection;

    /** Creates a new instance of MessageParserImpl */
    public MessageParserImpl(ORB orb) {
        this.orb = orb;
        this.expectingMoreData = false;
        this.moreBytesToParse = false;
        this.nextMsgStartPos = 0;
        this.fragmentList = new LinkedList<RequestId>();
        this.sizeNeeded = orb.getORBData().getReadByteBufferSize();
    }

    public MessageParserImpl(ORB orb, Connection connection) {
        this(orb);
        this.connection = connection;
    }

    @Transport
    public ByteBuffer getNewBufferAndCopyOld(ByteBuffer byteBuffer) {
        ByteBuffer newByteBuffer = null;
        // Set byteBuffer position to the start position of data to be
        // copied into the re-allocated ByteBuffer.
        byteBuffer.position(getNextMessageStartPosition());
        newByteBuffer = orb.getByteBufferPool().reAllocate(byteBuffer,
                getSizeNeeded());
        setNextMessageStartPosition(0);
        return newByteBuffer;
    }

    /**
     * Is this MessageParser expecting more data ?
     * @return - True if more bytes are needed to construct at least one
     *           GIOP protocol data unit.  False, if no additional bytes are
     *           remain to be parsed into a GIOP protocol data unit.
     */
    public boolean isExpectingMoreData() {
        return expectingMoreData;
    }

    @Override
    public ByteBuffer getMsgByteBuffer() {
        return msgByteBuffer;
    }

    @Override
    public void offerBuffer(ByteBuffer buffer) {
        msgByteBuffer = null;
        if (!containsFullHeader(buffer))
            remainderBuffer = buffer;
        else if (!containsFullMessage(buffer))
            remainderBuffer = buffer;
        else {
            remainderBuffer = splitAndReturnRemainder(buffer, getTotalMessageLength(buffer));
            MessageBase message = MessageBase.parseGiopHeader(orb, connection, buffer, 0);
            messageMediator = new MessageMediatorImpl(orb, connection, message, buffer);
            msgByteBuffer = buffer;
        }

    }

    /**
     * Splits the specified buffer at the specified position, returning the first part and
     * setting {@link #remainderBuffer} to the second, or null if there is no data in the second.
     * The split position must be no greater than the limit.
     */
    private ByteBuffer splitAndReturnRemainder(ByteBuffer buffer, int splitPosition) {
        assert splitPosition <= buffer.limit();

        if (buffer.limit() == splitPosition)
            return null;
        else {
            final int oldPosition = buffer.position();
            buffer.position(splitPosition);
            ByteBuffer remainderBuffer = buffer.slice();
            buffer.position(oldPosition);
            buffer.limit(splitPosition);
            return remainderBuffer;
        }
    }

    private boolean containsFullHeader(ByteBuffer buffer) {
        return buffer.remaining() >= Message.GIOPMessageHeaderLength;
    }

    private boolean containsFullMessage(ByteBuffer buffer) {
        return buffer.remaining() >= getTotalMessageLength(buffer);
    }

    private int getTotalMessageLength(ByteBuffer buffer) {
        return Message.GIOPMessageHeaderLength + getMessageBodyLength(buffer);
    }

    private int getMessageBodyLength(ByteBuffer buffer) {
        return buffer.getInt(MESSAGE_LENGTH_INDEX);
    }

    @Override
    public ByteBuffer getRemainderBuffer() {
        return remainderBuffer;
    }

    @Override
    public MessageMediator getMessageMediator() {
        return messageMediator;
    }


    @Transport
    public Message parseBytes(ByteBuffer byteBuffer, Connection connection) {
        expectingMoreData = false;
        remainderBuffer  = byteBuffer;
        Message message = null;
        int bytesInBuffer = byteBuffer.limit() - nextMsgStartPos;
        // is there enough bytes available for a message header?
        if (bytesInBuffer >= Message.GIOPMessageHeaderLength) {
            // get message header
            message = MessageBase.parseGiopHeader(orb, connection, byteBuffer, nextMsgStartPos);
            
            // is there enough bytes for a message body?
            if (bytesInBuffer >= message.getSize()) {

                // slice the ByteBuffer into a GIOP PDU
                int savedLimit = byteBuffer.limit();
                byteBuffer.position(nextMsgStartPos).
                        limit(nextMsgStartPos + message.getSize());
                msgByteBuffer = byteBuffer.slice();
                // update nextMsgStartPos and byteBuffer state
                nextMsgStartPos = byteBuffer.limit();
                byteBuffer.position(nextMsgStartPos).limit(savedLimit);

                if (message.supportsFragments()) {
                    if (message.moreFragmentsToFollow()) {
                        addRequestIdToFragmentList(message, msgByteBuffer);
                    } else if (isEndOfFragmentList(message)) {
                        removeRequestIdFromFragmentList(message, msgByteBuffer);
                    }
                    expectingMoreData = stillLookingForFragments();
                }

                moreBytesToParse = byteBuffer.hasRemaining();
                if (!moreBytesToParse) byteBuffer.limit(byteBuffer.capacity());
                sizeNeeded = orb.getORBData().getReadByteBufferSize();
            } else {
                // set state for next parseBytes invocation
                moreBytesToParse = false;
                expectingMoreData = true;
                // nextMsgStartPos unchanged
                byteBuffer.position(byteBuffer.limit()).limit(byteBuffer.capacity());
                sizeNeeded = message.getSize();
                message = null;
            }
        } else {
            // not enough bytes for message header
            // set state for next parseBytes invocation
            moreBytesToParse = false;
            expectingMoreData = true;
            // nextMsgStartPos unchanged
            byteBuffer.position(byteBuffer.limit()).limit(byteBuffer.capacity());
            sizeNeeded = orb.getORBData().getReadByteBufferSize();
        }
        return message;
    }

    private boolean stillLookingForFragments() {
        return fragmentList.size() > 0;
    }

    private boolean isEndOfFragmentList(Message message) {
        return message.getType() == MessageBase.GIOPFragment ||
            message.getType() == MessageBase.GIOPCancelRequest;
    }

    private void removeRequestIdFromFragmentList(Message message, ByteBuffer byteBuffer) {
        // remove request id from fragmentList
        RequestId requestId = MessageBase.getRequestIdFromMessageBytes(message, byteBuffer);
        if (fragmentList.size() > 0 &&
            fragmentList.remove(requestId)) {
        }
    }

    private void addRequestIdToFragmentList(Message message, ByteBuffer byteBuffer) {
        // Add to fragmentList if not already there
        RequestId requestId = MessageBase.getRequestIdFromMessageBytes(message, byteBuffer);
        if (!fragmentList.contains(requestId)) {
            fragmentList.add(requestId);
        }
    }

    /**
     * Are there more bytes to be parsed in the <code>ByteBuffer</code> given
     * to this MessageParser's <code>parseBytes</code> ?
     *
     * This method is typically called after a call to <code>parseBytes()</code>
     * to determine if the <code>ByteBuffer</code> has more bytes which need to
     * parsed into a <code>Message</code>.
     *
     * @return <code>true</code> if there are more bytes to be parsed.
     *         Otherwise <code>false</code>.
     */
    public boolean hasMoreBytesToParse() {
        return moreBytesToParse;
    }
    
    /**
     * Set the starting position where the next message in the
     * <code>ByteBuffer</code> given to <code>parseBytes()</code> begins.
     */
    public void setNextMessageStartPosition(int position) {
        this.nextMsgStartPos = position;
    }
    
    /**
     * Get the starting position where the next message in the
     * <code>ByteBuffer</code> given to <code>parseBytes()</code> begins.
     */
    public int getNextMessageStartPosition() {
        return this.nextMsgStartPos;
    }
    
    /** Return a string representing this MessageParser's state */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(toStringPrefix()).append("]");
        return sb.toString();
    }

    /** Return a common String prefix representing this MessageParser's state */
    private String toStringPrefix() {
        StringBuilder sb = new StringBuilder();
        sb.append("MessageParserImpl[nextMsgStartPos=").append(nextMsgStartPos).append(", expectingMoreData=").append(expectingMoreData);
        sb.append(", moreBytesToParse=").append(moreBytesToParse).append(", fragmentList size=").append(fragmentList.size());
        sb.append(", size needed=").append(sizeNeeded).append("]");
        return sb.toString();
    }

    /**
     * Return the suggested number of bytes needed to hold the next message
     * to be parsed.
     */
    public int getSizeNeeded() {
        return sizeNeeded;
    }
}
