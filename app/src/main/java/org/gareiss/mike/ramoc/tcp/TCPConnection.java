package org.gareiss.mike.ramoc.tcp;

import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.gareiss.mike.ramoc.tv.htsp.HTSConnection.TIMEOUT_ERROR;

/**
 * Created by drue on 23.02.17.
 */

public class TCPConnection extends Thread
{
    public static final int CONNECTED = 1;
    public static final int CONNECTION_REFUSED_ERROR = 2;
    public static final int DISCONNECTED = 3;

    private Lock lock;
    private TCPConnectionListener listener = null;
    private String SERVERIP;
    public static final int SERVERPORT = 4444;
    private String TAG = "TCPConnection";
    private Boolean mRun = false;
    private SocketChannel socketChannel ;
    private Selector selector;
    private ByteBuffer inBuf;
    private LinkedList<String> messageQueue;
    private ByteBuffer outBuffer;
    private String tcp_String;
    /**
     *  Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public TCPConnection(String string_RaspberryIP, TCPConnectionListener listener)
    {
        this.listener = listener;
        SERVERIP = string_RaspberryIP;
        lock = new ReentrantLock();
        messageQueue = new LinkedList<String>();
        inBuf = ByteBuffer.allocateDirect(1024);
        try {
            socketChannel = SocketChannel.open();
        }
        catch(Exception ex)
        {

        }
        tcp_String = "";
    }

    // Creates a non-blocking socket channel for the specified host name and port.
    // connect() is called on the new channel before it is returned.
    /*public static SocketChannel createSocketChannel(String hostName, int port) throws IOException
    {
        // Create a non-blocking socket channel
        SocketChannel sChannel = SocketChannel.open();
        sChannel.configureBlocking(false);

        // Send a connection request to the server; this method is non-blocking
        sChannel.connect(new InetSocketAddress(hostName, port));
        return sChannel;
    }*/

    /**
     * Sends the message entered by client to the server
     */
    public void sendMessage(String message)
    {

        /*if(!isConnected())
        {
            listener.onError(DISCONNECTED);
            reconnect();
            return;
        }*/

        try
        {
            socketChannel.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ | SelectionKey.OP_CONNECT);
            messageQueue.add(message);
            selector.wakeup();
        }
        catch (Exception ex)
        {
            Log.e(TAG, "Can't transmit message", ex);
            this.listener.onError(ex);
        }
    }

    public void stopClient()
    {
        Log.i(TAG, "stopClient");
        try
        {
            mRun = false;
            socketChannel.close();
            socketChannel.register(selector, 0);
        }
        catch (IOException e)
        {
            Log.e(TAG, "Can't close connection", e);
        }
    }

    public Boolean isConnected()
    {

        return socketChannel != null
                && socketChannel.isOpen()
                && socketChannel.isConnected()
                && mRun;
        //return socketChannel.isConnected();
    }

    public void connect()
    {
        //if (mRun)
        if( isConnected())
        {
            return;
        }

        final Object signal = new Object();

        try
        {
            // Create the selector
            selector = Selector.open();

            socketChannel.configureBlocking(false);
            socketChannel.socket().setKeepAlive(true);
            socketChannel.socket().setSoTimeout(5000);
            socketChannel.register(selector, SelectionKey.OP_CONNECT, signal);
            socketChannel.connect(new InetSocketAddress(SERVERIP, 4444));

                messageQueue.add("001 Get Settings");
                Log.i(TAG, "messageQueue:" + Integer.toString(messageQueue.size()));
                mRun = true;
                start();
                listener.onError(CONNECTED);

        }
        catch (Exception ex)
        {
            Log.e(TAG, "Can't open connection", ex);
            listener.onError(CONNECTION_REFUSED_ERROR);
            return;
        }

        synchronized (signal)
        {
            try
            {
                signal.wait(5000);
                if (socketChannel.isConnectionPending())
                {
                    listener.onError(TIMEOUT_ERROR);
                    stopClient();
                }
            }
            catch (InterruptedException ex)
            {
            }
        }
    }


    public void reconnect(String string_RaspberryIP)
    {
        SERVERIP = string_RaspberryIP;

        Log.i(TAG, "reconnect: " + SERVERIP);
        if(!socketChannel.isOpen())
        {
            try {
                socketChannel = SocketChannel.open();
            }
            catch(Exception ex)
            {

            }
        }
        connect();
    }

    @Override
    public void run()
    {
        Log.i(TAG, "run()");

        while(mRun)
        {
            try {
                // Wait for an event
                selector.select(5000);
            } catch (IOException e) {
                // Handle error with selector
                //break;
                continue;
            }

            lock.lock();

            try
            {
                Iterator it = selector.selectedKeys().iterator();
                while (it.hasNext())
                {
                    SelectionKey selKey = (SelectionKey) it.next();
                    it.remove();
                    processSelectionKey(selKey);
                }

                int ops = SelectionKey.OP_READ;
                if (!messageQueue.isEmpty())
                {
                    ops |= SelectionKey.OP_WRITE;
                }
                socketChannel.register(selector, ops);
            }
            catch (Exception ex)
            {
                Log.e(TAG, "Can't read message", ex);
                //listener.onError(ex);
                mRun = false;
                stopClient();
            }
            finally
            {
                lock.unlock();
            }
            //mRun = isConnected();
        }
        Log.i(TAG, "Ende run()");
    }

    public void processSelectionKey(SelectionKey selKey) throws IOException
    {
        // Since the ready operations are cumulative,
        // need to check readiness for each operation
        if (selKey.isValid() && selKey.isConnectable())
        {
            // Get channel with connection request
            SocketChannel sChannel = (SocketChannel)selKey.channel();

            boolean success = sChannel.finishConnect();
            if (!success)
            {
                // Unregister the channel with this selector
                selKey.cancel();
            }
            else
            {
                Log.i(TAG, "succes");
            }
        }

        if (selKey.isValid() && selKey.isReadable())
        {
            String message;

            // Get channel with bytes to read
            SocketChannel sChannel = (SocketChannel)selKey.channel();
            inBuf.clear();
            inBuf.position(0);
            int len = sChannel.read(inBuf);
            inBuf.flip();
            byte[] subStringBytes = new byte[len];

            System.arraycopy( inBuf.array(), 4, subStringBytes, 0, len );
            message = new String(subStringBytes, "UTF-8");
            tcp_String = tcp_String + message;

            Log.i(TAG, "gelesen: " + message);
            Log.i(TAG, Integer.toString(message.length()));

            if(tcp_String.endsWith("\n"))
            {
                listener.onMessage(tcp_String);
                //if(tcp_String.startsWith("001"))
                //    stopClient();
                tcp_String = "";
            }
        }

        if (selKey.isValid() && selKey.isWritable())
        {
            // Get channel that's ready for more bytes
            SocketChannel sChannel = (SocketChannel)selKey.channel();
            String msg = messageQueue.poll();
            if(msg != null)
            {
                Log.i(TAG, msg);

                outBuffer = ByteBuffer.allocateDirect(msg.getBytes("UTF-8").length);
                outBuffer.put(msg.getBytes("UTF-8"));
                outBuffer.flip();
                int numBytesWritten = sChannel.write(outBuffer);
                //Log.i(TAG, "Buffergrösse: " + Integer.toString(outBuffer.array().length));
                //Log.i(TAG, "geschriebene Bytes: " + Integer.toString(numBytesWritten));
               // outBuffer.flip();
            }
            // See Writing to a SocketChannel
        }
    }

}
