/**
 * Client
 */


import kotlinx.coroutines.*
import java.io.DataInputStream
import java.io.DataOutputStream
import java.lang.Exception
import java.net.Socket

class ClientManager(ip: String,port:Int) {

    private var ip: String = ""
    private var port: Int = 0
    /**
     * isC
     * 0 -> wait for connect
     * 1 -> connected
     * 2 -> failed
     */
    private var isC = 0
    private var scope = GlobalScope
    private var sc: Socket? = null
    private var inS: DataInputStream? = null
    private var outS: DataOutputStream? = null

    init {
        this.ip = ip
        this.port = port
    }

    fun initClient(result: ((Int) -> Unit),
                   onReceive: (String) -> Unit) {
        scope.launch(Dispatchers.IO) {
            try {
                //init Socket
                sc = Socket(ip,port)
                //receive date
                inS = DataInputStream(sc!!.getInputStream())
                //send date
                outS = DataOutputStream(sc!!.getOutputStream())
                if (sc!!.isConnected) {
                    isC = 1
                    this.receiveMessage(sc!!,onReceive)
                    println("connect to"+sc!!.inetAddress+":"+sc!!.localPort);
                }
                withContext(Dispatchers.Main){
                    result.invoke(isC)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun sendMessage(mess: String){
        scope.launch(Dispatchers.IO) {
            try {
                outS!!.writeUTF(mess)
                outS!!.flush()
            }catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun CoroutineScope.receiveMessage(
            sc: Socket,
            onReceive:((String) -> Unit)
    ): Job {
        return this.launch(Dispatchers.IO) {
            var mR: String? = null
            sc.apply {
                if (sc.isClosed) {
                    withContext(Dispatchers.Main) {
                        //base.onReceiveMessageFail()
                    }
                }else{
                    while (!sc.isClosed){
                        try {
                            mR = inS!!.readUTF()
                            if (mR != null){
                                withContext(Dispatchers.Main){
                                    onReceive.invoke(mR!!)
                                }
                            }
                        }
                        catch (e: Exception) {
                            e.printStackTrace()
                            withContext(Dispatchers.Main){
                                //base.onReceiveMessageFail()
                            }
                        }
                    }
                }
            }
        }
    }

    fun closeConnection(){
        sc!!.close()
        inS!!.close()
        outS!!.close()
        isC = 0
        println("-----------Server Ended-----------")
    }

}



/**
 * Server
 */

import kotlinx.coroutines.*
import java.io.DataInputStream
import java.io.DataOutputStream
import java.lang.Exception
import java.net.ServerSocket
import java.net.Socket

class ServerManager (port: Int) {

    private var port: Int = 0
    /**
     * isC
     * 0 -> wait for connect
     * 1 -> connected
     * 2 -> failed
     */
    private var isC = 0
    private val scope = GlobalScope
    private var server: ServerSocket? = null
    private val socketMap = HashMap<String,Socket>()
    private val outputSMap = HashMap<String,DataOutputStream>()

    init {
        this.port = port
    }

    fun initServer(result: ((Int) -> Unit),
                   onReceive: (String) -> Unit)
    {
        scope.launch(Dispatchers.IO) {
            try {
                //init Socket
                server = ServerSocket(port)
                while (!server!!.isClosed){
                    val sc = server!!.accept()

                    if (sc!!.isConnected) {
                        isC = 1
                        socketMap[sc.inetAddress.toString()] = sc
                        outputSMap[sc.inetAddress.toString()] = DataOutputStream(sc.getOutputStream())
                        this.receiveMessage(sc,onReceive)
                        println("connect to"+sc.inetAddress+":"+sc.localPort)
                    }else {
                        isC = 2
                        println("fail to connect!!!!")
                    }
                    withContext(Dispatchers.Main){
                        result.invoke(isC)
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                isC = 2
            }
        }
    }

    fun sendMessage(msg: String,
                    address: String,
                    result: (String?) -> Unit)
    {
        scope.launch(Dispatchers.IO) {
            try {
                if (outputSMap[address] != null) {
                    outputSMap[address]!!.writeUTF(msg)
                    outputSMap[address]!!.flush()
                }else{
                    withContext(Dispatchers.Main){
                        //TODO:have not connected
                    }
                }
                withContext(Dispatchers.Main){
                    result.invoke(msg)
                }
            }catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main){
                    result.invoke(null)
                }
            }
        }
    }


    private fun CoroutineScope.receiveMessage(
            sc: Socket,
            onReceive:((String) -> Unit)
    ): Job {
        return this.launch(Dispatchers.IO) {
            var mR: String?
            val inS = DataInputStream(sc.getInputStream())
            sc.apply {
                if (sc.isClosed) {
                    withContext(Dispatchers.Main) {
                        //TODO:socket is closed
                    }
                }else{
                    while (!sc.isClosed){
                        try {
                            mR = inS.readUTF()
                            if (mR != null){
                                withContext(Dispatchers.Main){
                                    onReceive.invoke(mR!!)
                                }
                            }
                        }
                        catch (e: Exception) {
                            e.printStackTrace()
                            withContext(Dispatchers.Main){
                                //TODO:add a receive message callback
                            }
                        }
                    }
                }
            }
        }
    }

    fun closeConnection(){
        server?.close()
        scope.cancel()
        socketMap.forEach { it.value.close() }
        outputSMap.forEach{ it.value.close() }
        socketMap.clear()
        outputSMap.clear()
        isC = 0
        println("-----------Server Ended-----------")
    }

}