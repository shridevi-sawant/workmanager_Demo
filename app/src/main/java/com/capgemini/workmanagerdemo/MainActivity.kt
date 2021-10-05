package com.capgemini.workmanagerdemo

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.work.*

class MainActivity : AppCompatActivity() {

    lateinit var request: OneTimeWorkRequest
    lateinit var wManager : WorkManager

    lateinit var statusTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        statusTextView = findViewById(R.id.tv)

        wManager = WorkManager.getInstance(this)
        scheduleTask()
    }

    private fun scheduleTask() {

       val builder = OneTimeWorkRequestBuilder<DataSplitter>()

       val inputData = Data.Builder()
            .putString("longString", "mumbai-delhi-bangalore-chennai-hyderabad")
            .build()

        builder.setInputData(inputData)

        val wConstraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        builder.setConstraints(wConstraints)

        request = builder.build()


        wManager.enqueue(request)

        wManager.getWorkInfoByIdLiveData(request.id).observe(this, Observer {
            var status = ""
            when(it.state){
                WorkInfo.State.ENQUEUED -> status = "Enqueued"
                WorkInfo.State.RUNNING -> status = "Running"
                WorkInfo.State.CANCELLED -> status = "Cancelled"
                WorkInfo.State.FAILED -> status = "failed"
                WorkInfo.State.SUCCEEDED -> {
                    status = "Success"

                    val splits = it.outputData.getStringArray("parts")
                    statusTextView.append("\nResult: ${splits.contentToString()}")
                }
                WorkInfo.State.BLOCKED -> status = "blocked"
            }
            statusTextView.append("\n$status")
        })
    }


    // task input- "mumbai-delhi-bangalore"
    // task output - spliited strings
    class DataSplitter(ctx: Context, params: WorkerParameters) : Worker(ctx, params){
        override fun doWork(): Result {
            // defferrable task execution

            val inpData = inputData.getString("longString")
            Log.d("MainActivity", "Work execution with input: $inpData ")

            val splits = inpData?.split("-")!!

            val outData = Data.Builder()
                .putStringArray("parts", splits.toTypedArray())
                .build()

            Log.d("MainActivity", "Work output: $splits ")
            return Result.success(outData)
        }

    }
}