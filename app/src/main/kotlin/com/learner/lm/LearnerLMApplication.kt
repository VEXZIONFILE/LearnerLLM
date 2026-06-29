package com.learner.lm

import android.app.Application
import com.learner.lm.database.LearnerDatabase

class LearnerLMApplication : Application() {
    val database: LearnerDatabase by lazy { LearnerDatabase.getInstance(this) }
}
