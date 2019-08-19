package com.mrntlu.kotlincoroutinesexample

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ProgressBar
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_jobs.*
import kotlinx.android.synthetic.main.activity_jobs.job_button
import kotlinx.android.synthetic.main.activity_jobs.view.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main

class JobsActivity : AppCompatActivity() {

    private val TAG: String = "AppDebug"

    private val PROGRESS_MAX = 100
    private val PROGRESS_START = 0
    private val JOB_TIME = 4000 // ms
    private lateinit var job: CompletableJob

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jobs)

        job_button.setOnClickListener {
            if (!::job.isInitialized){
                initJob()
            }
            job_progress_bar.startJobOrCancel(job)
        }
    }

    fun ProgressBar.startJobOrCancel(job:Job){
        if (this.progress>0){
            println("$job is already active. Cancelling...")
            resetJob()
        }else{
            this@JobsActivity.job_button.text = "Cancel job #1"
            CoroutineScope(IO + job).launch {//Without +Job it would cancel all in IO but with Job you can just cancel that job
                Log.d(TAG, "coroutine ${this} is activated with job ${job}.")

                for (i in PROGRESS_START..PROGRESS_MAX){
                    delay((JOB_TIME/PROGRESS_MAX).toLong())
                    this@startJobOrCancel.progress=i
                }
                updateJobCompleteTextView("Job is complete")
            }
        }
    }

    fun updateJobCompleteTextView(text: String){
        GlobalScope.launch(Main){
            job_complete_text.text=text
        }
    }

    fun resetJob() {
        if (job.isActive || job.isCompleted){
            job.cancel(CancellationException(("Resetting job")))// If a job canceled you can reuse it.
        }
        initJob()
    }

    fun initJob(){
        job_button.text="Start Job#1"
        updateJobCompleteTextView("")
        job= Job()
        job.invokeOnCompletion {
            it?.message.let{
                var msg=it
                if (msg.isNullOrBlank()){
                    msg="Unknown cancellation error."
                }
                Log.e(TAG, "${job} was cancelled. Reason: ${msg}")
                showToast(msg)
            }
        }
        job_progress_bar.max=PROGRESS_MAX
        job_progress_bar.progress=PROGRESS_START
    }

    fun showToast(text:String){
        GlobalScope.launch(Main) {
            Toast.makeText(this@JobsActivity,text,Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
