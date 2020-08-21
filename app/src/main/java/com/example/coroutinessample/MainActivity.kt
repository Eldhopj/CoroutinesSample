package com.example.coroutinessample

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*

/**
 * Difference with threads
 * 1. Executes inside a thread, we can run multiple coroutines inside a single thread
 * 2. coroutines are suspendable, it pause and continue when we wanted to
 * 3. Can switch content, ie, it can switch the thread to another*/

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        globalScope()
//        localScope()
//        dispatcherContexts()
//        switchContexts()
//        sequentialOperations()
//        jobCancellation()
        postCompletion()
//        blockMainThread()
    }

    /**
     * In global scope coroutine life is same as application life
     * If coroutine finishes its job it will be destroyed
     * NOTE : Coroutines launching from GlobalScope is not a good idea,
     * */
    private fun globalScope() {
        GlobalScope.launch(Dispatchers.Default) {
            delayFun()
            Log.d(TAG, "Global scope thread: ${Thread.currentThread().name}")
        }
    }

    /**
     * Scope of Activity, Fragment, View or ViewModel lifecycle
     * */
    private fun localScope() {
        CoroutineScope(Dispatchers.Main).launch {
            delayFun()
            Log.d(TAG, "Dispatcher context thread: ${Thread.currentThread().name}")
        }
    }

    /**
     * suspending function is simply a function that can be paused and resumed at a later time
     * suspend fun only can be executed inside a coroutine or another suspend func
     * */
    private suspend fun delayFun() {
        delay(3000L) // pause the current coroutine for 3sec
    }

    /**
     * Based on what our coroutines do we have to chose dispatcher
     * .Main - > dispatcher that is confined to the Main thread operating with UI objects.
     *           NOTE : running something on main thread wont block the UI thread
     * .IO - > For all kinds of data operations like n/w calls, read/write of files etc
     * .Default -> complex long running operations
     * .Unconfined - > its not confined to any specific thread (TODO :Usage to find out)
     * */
    private fun dispatcherContexts() {
        CoroutineScope(Dispatchers.Main) // Dispatchers
            .launch {
                delayFun()
                Log.d(TAG, "Dispatcher context thread: ${Thread.currentThread().name}")
            }
    }

    /**
     * withContext() -> used to switch the context of coroutine*/
    private fun switchContexts() {
        CoroutineScope(Dispatchers.IO).launch {
            Log.d(TAG, "Switch context I/O thread: ${Thread.currentThread().name}")
            val result = dummyNetworkCall()

            //Switching to main tread to do UI operation
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, result, Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Switch context Main thread: ${Thread.currentThread().name}")
            }
        }
    }

    /**
     * runBlocking is sync in nature
     * useful when we do junit testing
     * */
    private fun blockMainThread() {
        runBlocking {
            delayFun()
        }
    }

    /**
     * async() -> executes a task in a coroutineScope and returns the result.
     * await() -> waits for that result and gets the value from it, the coroutine where await() is used it will pause for the result until then
     * */
    private fun sequentialOperations() {
        CoroutineScope(Dispatchers.Default).launch { // instead of launch we can use async

            val job1 = async {
                job1()
            }

            val job2 = async {
                fibnoJob(3)
                job2(job1.await()) //tread will pause for the job1 result after that only the code below will execute
            }

            Log.d(TAG, "parallelOperations: ${job1.await()} ${job2.await()} \n")
        }
    }


    /**
     * 1.We can cancel job using .cancel() fun and when we using this fun we have to check the job isActive or not
     * 2.Second method is that using withTimeout : job will stop exe after a certain time
     * */
    private fun jobCancellation() {
        CoroutineScope(Dispatchers.Default).launch {
            val job = jobForCancellation()
            delayFun()
            job.cancel() // cancelling of job
            Log.d(TAG, " job cancelled ")
        }
    }

    private fun jobForCancellation(): Job {
        return CoroutineScope(Dispatchers.Default).launch {
            Log.d(TAG, "jobStarted: ")
            /**Timeout: job will stop exe after a certain time*/
            withTimeout(2000L) {
                for (i in 5..50) {
                    /**Important check : Checks whether the job is cancelled or not,
                     *  not needed if u only using withTimeout() */
                    if (isActive)
                        Log.d(TAG, "fibno result: ${fibnoJob(i)}")
                }
            }
        }
    }

    /**
     * After the job is completed invokeOnCompletion will be called*/
    private fun postCompletion() {
        val job = CoroutineScope(Dispatchers.Default).launch {
            delayFun()
        }

        job.invokeOnCompletion {
            Log.d(TAG, "On post job completion: ")
        }
    }


    //---------------------------------- mock data's---------------------------------------/

    private suspend fun dummyNetworkCall(): String {
        delay(10000L) // mocking network call
        return "Operation Completed"
    }

    private suspend fun job1(): String {
        delay(2000L) // mocking job
        Log.d(TAG, "job 1 finished: ")
        return "\n Job finished"
    }

    private suspend fun job2(data: String): String {
        delay(100L) // mocking job
        Log.d(TAG, "job 2 finished: ")
        return "$data 2"
    }

    private fun fibnoJob(n: Int): Long {
        return when (n) {
            0 -> 0
            1 -> 1
            else -> (fibnoJob(n - 1) + fibnoJob(n - 2))
        }
    }

}