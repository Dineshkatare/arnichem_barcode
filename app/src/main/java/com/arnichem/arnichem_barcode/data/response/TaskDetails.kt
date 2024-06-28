package com.example.myapplication.data.response

data class TaskDetails(
    val srno: Int,
    val user: String,
    val dateAdded: String,
    val description: String,
    val effort: String? = null, // Add other main task details
    val priority: String? = null,
    val status: String? = null,
    val category: String? = null,
    val remarks: String? = null,
    val due: String? = null,
    val dateCompleted: String? = null,
    val taskDetails: List<TaskDetail>
)

data class TaskDetail(
    val srno: Int,
    val taskSrno: Int,
    val date: String,
    val detail: String
)
