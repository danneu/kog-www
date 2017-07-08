package com.danneu.kogwww

import com.danneu.kog.Env
import com.danneu.kog.Handler
import com.danneu.kog.Response
import com.danneu.kog.Router
import com.danneu.kog.Server
import com.danneu.kog.batteries.logger
import com.danneu.kog.batteries.serveStatic
import com.danneu.kog.middleware.compose
import com.danneu.kogwww.Environment.Development
import com.danneu.kogwww.Environment.Production
import com.mitchellbosecke.pebble.PebbleEngine
import com.mitchellbosecke.pebble.template.PebbleTemplate
import java.io.StringWriter
import java.time.Duration

val engine: PebbleEngine = PebbleEngine.Builder()
    .apply {
        if (Config.KOG_ENV == Development) {
            println("Disabling Pebble cache in development")
            // FIXME: This doesn't seem to work -- I need to recompile to pickup template changes.
            this.cacheActive(false)
        }
    }
    .build()

fun Response.render(path: String, data: HashMap<String, Any> = hashMapOf()): Response {
    val template: PebbleTemplate = engine.getTemplate(path)

    if (Config.KOG_ENV == Production) {
        return Response().writer("text/html") { template.evaluate(it, data) }
    }

    return StringWriter().apply { template.evaluate(this, data) }.toString().let { html ->
        Response().html(html)
    }
}

val router = Router {
    get("/", fun(): Handler = {
        Response().render("pebble/homepage.peb")
    })
    get("/guide", fun(): Handler = {
        Response().redirect("https://github.com/danneu/kog")
    })
}

fun main(args: Array<String>) {
    val middleware = compose(
        serveStatic("public", maxAge = Duration.ofDays(365)),
        if (Config.KOG_ENV == Development) logger() else null
    )

    Server(middleware(router.handler())).listen(Env.int("PORT") ?: 3000)
}

