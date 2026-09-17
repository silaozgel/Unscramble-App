package com.unscramble.app.model

object UnscrambleGameData {
    val wordsTr = mapOf(
        "kolay" to listOf("kedi", "köpek", "makam", "mevlüt", "ağaç", "balık", "sabun", "forma", "kek", "kapı",
            "yorgun", "akran", "dergi", "masa", "mavi", "hakem", "ekmek", "bal", "tablo", "cimri"),

        "orta" to listOf("bilgisayar", "çekirge", "sandalye", "voleybol", "delikanlı", "bardak", "pelikan", "mütevazı",
            "müzik", "menekşe", "okyanus", "profesör", "nehir", "gardırop", "istasyon", "turizm", "deniz", "kalem",
            "defter", "kahvaltı"),

        "zor" to listOf("ansiklopedi", "başkalaşım", "cumhuriyet", "değerlendirme", "elektromanyetik", "faydalanmak",
            "geleneksel", "hayalperest", "istikrar", "jeopolitik", "karşılaştırma", "laboratuvar", "medeniyet",
            "normalleşme", "organizasyon", "programlama", "radyoaktivite", "sürdürülebilir", "teknoloji", "uzmanlaşmak")

    )

    val wordsEn = mapOf(
        "kolay" to listOf("cat", "dog", "sun", "book", "tree", "fish", "ball", "bird", "cake", "door", "frog", "hand",
                    "lamp", "sharp", "milk", "star", "desk", "blue", "red", "gold"),

        "orta" to listOf("apple", "bread", "chair", "dance", "space", "fruit", "glass", "grace", "juice", "lemon",
            "music", "slight", "ocean", "paper", "queen", "river", "breakfast", "table", "water", "young"),

        "zor" to listOf("astronomy", "squirrel", "cryptography", "destination", "serendipity", "journey", "rhythm",
            "climate", "innovation", "curious", "solution", "labyrinth", "metaphorical", "strategy", "orchestra",
            "photography", "quarantine", "analyse", "spectacular", "evidence")

    )
}
