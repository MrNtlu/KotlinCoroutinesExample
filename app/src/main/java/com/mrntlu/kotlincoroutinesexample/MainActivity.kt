package com.mrntlu.kotlincoroutinesexample

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    private val JOB_TIMEOUT=1900L
    private val JOB_TIMEOUT_2=2100L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button.setOnClickListener {
            setNewText("Click!")

            //IO(Network and local db interactions), Main, Default(Heavy computational work)
            CoroutineScope(IO).launch {
                fakeApiRequest()
            }
        }

        activityButton.setOnClickListener {
            startActivity(Intent(it.context,JobsActivity::class.java))
        }
    }

    private fun setNewText(input: String){
        val newText = text.text.toString() + "\n$input"
        text.text = newText
    }
    private suspend fun setTextOnMainThread(input: String) {
        withContext (Main) {
            setNewText(input)
        }
    }

    private suspend fun fakeApiRequest() {
        withContext(IO) {
            val job = withTimeoutOrNull(JOB_TIMEOUT) {
                val result1=getResult1FromApi() // wait 1000ms
                setTextOnMainThread("Got $result1")

                val result2=getResult2FromApi() // wait 1000ms
                setTextOnMainThread("Got $result2")
            }// waits until complitation

            if (job==null){ // if it did timeout
                val cancelMessage="Canceling message... Job took longer than $JOB_TIMEOUT ms"
                println("debug: $cancelMessage")
                setTextOnMainThread(cancelMessage)
            }

            val job2=launch {

            }
            job2.join()
            job2.cancel("Cancelation exception")
        }
        /*logThread("fakeApiRequest")

        val result1 = getResult1FromApi() // wait until job is done

        if ( result1.equals("Result #1")) {

            setTextOnMainThread("Got $result1")

            val result2 = getResult2FromApi() // wait until job is done

            if (result2.equals("Result #2")) {
                setTextOnMainThread("Got $result2")
            } else {
                setTextOnMainThread("Couldn't get Result #2")
            }
        } else {
            setTextOnMainThread("Couldn't get Result #1")
        }*/
    }


    private suspend fun getResult1FromApi(): String {
        logThread("getResult1FromApi")
        delay(1000) // Does not block thread. Just suspends the coroutine inside the thread
        return "Result #1"
    }

    private suspend fun getResult2FromApi(): String {
        logThread("getResult2FromApi")
        delay(1000)
        return "Result #2"
    }

    private fun logThread(methodName: String){
        println("debug: ${methodName}: ${Thread.currentThread().name}")
    }
}
