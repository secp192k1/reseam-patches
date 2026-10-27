package dev.secp192k1.patches.brand

import app.reseam.patch.settings.About
import app.reseam.patch.settings.Link
import app.reseam.patch.settings.Verbatim

// Shared modules have no string table, so the labels stay verbatim like SETTINGS_TITLE
val publisherLinks =
    About(
        links =
            listOf(
                Link(Verbatim("Discord"), "https://discord.com/users/477497542205243392", lucide("message-circle")),
                Link(Verbatim("Repository"), "https://github.com/secp192k1/reseam-patches", lucide("folder-git-2")),
            ),
    )
