package com.danneu.kogwww

import com.danneu.kog.Env
import com.danneu.kogwww.Environment.Development
import com.danneu.kogwww.Environment.Production

enum class Environment { Development, Production }

object Config {
    val KOG_ENV = if (Env.string("KOG_ENV") == "production") {
        Production
    } else {
        Development
    }
}


