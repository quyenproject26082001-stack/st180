package com.xylophone.learning.music.data.model

object SongLibrary {

    fun getallSongs(): List<Song> {
        return listOf(

            // ==================== EASY (10 bài) ====================

            Song(
                id = "baby_shark",
                name = "Baby Shark",
                notes = listOf(
                    // Baby shark doo doo doo (x3)
                    "Do","Re","Fa","Fa","Fa","Fa","Fa","Fa","Fa",
                    "Do","Re","Fa","Fa","Fa","Fa","Fa","Fa","Fa",
                    "Do","Re","Fa","Fa","Fa","Fa","Fa","Fa","Fa","Fa","Fa","Mi",
                    // Baby shark doo doo doo (x3)
                    "Do","Re","Fa","Fa","Fa","Fa","Fa","Fa","Fa",
                    "Do","Re","Fa","Fa","Fa","Fa","Fa","Fa","Fa",
                    "Do","Re","Fa","Fa","Fa","Fa","Fa","Fa","Fa","Fa","Fa","Mi",
                    // Daddy shark (x3)
                    "Fa","Sol","Do2","Do2","Do2","Do2","Do2","Do2","Do2",
                    "Fa","Sol","Do2","Do2","Do2","Do2","Do2","Do2","Do2",
                    "Fa","Sol","Do2","Do2","Do2","Do2","Do2","Do2","Do2","Do2","Do2","Si",
                    // Daddy shark (x3)
                    "Fa","Sol","Do2","Do2","Do2","Do2","Do2","Do2","Do2",
                    "Fa","Sol","Do2","Do2","Do2","Do2","Do2","Do2","Do2",
                    "Fa","Sol","Do2","Do2","Do2","Do2","Do2","Do2","Do2","Do2","Do2","Si",
                ),
                description = "Most viewed YouTube video ever! 🦈"
            ),

            Song(
                id = "twinkle_star",
                name = "Twinkle Twinkle Little Star",
                notes = listOf(
                    // Twinkle twinkle little star how I wonder what you are
                    "Do","Do","Sol","Sol","La","La","Sol",
                    "Fa","Fa","Mi","Mi","Re","Re","Do",
                    // Up above the world so high like a diamond in the sky
                    "Sol","Sol","Fa","Fa","Mi","Mi","Re",
                    "Sol","Sol","Fa","Fa","Mi","Mi","Re",
                    // Twinkle twinkle little star how I wonder what you are
                    "Do","Do","Sol","Sol","La","La","Sol",
                    "Fa","Fa","Mi","Mi","Re","Re","Do"
                ),
                description = "Famous lullaby ⭐"
            ),

            Song(
                id = "wheels_on_bus",
                name = "Wheels on the Bus",
                notes = listOf(
                    // The wheels on the bus go round and round
                    "Do","Fa","Fa","Fa","Fa","La","Do2","La",
                    "Fa","Sol","Mi","Do","Do2","La","Fa","Do","Fa","Fa","Fa",
                    // (repeat)
                    "Fa","La","Do2","La","Fa","Sol","Do","Do","Fa",


                    // The wheels on the bus go round and round
                    "Do","Fa","Fa","Fa","Fa","La","Do2","La",
                    "Fa","Sol","Mi","Do","Do2","La","Fa","Do","Fa","Fa","Fa",
                    // (repeat)
                    "Fa","La","Do2","La","Fa","Sol","Do","Do","Fa",
                ),
                description = "Classic children's song 🚌"
            ),

            Song(
                id = "old_macdonald",
                name = "Old MacDonald Had a Farm",
                notes = listOf(
                    // Old MacDonald had a farm E-I-E-I-O
                    "Sol","Sol","Sol","Re","Mi","Mi","Re",
                    "Si","Si","La","La","Sol",
                    // And on his farm he had a cow E-I-E-I-O
                    "Re","Sol","Sol","Sol","Re","Mi","Mi",
                    "Re","Si","Si","La","La",
                    // With a moo moo here and a moo moo there
                    "Sol","Re","Re",
                    "Sol","Sol","Sol","Sol","Sol","Sol",
                    "Sol","Sol","Sol","Sol","Sol","Sol",
                    "Sol","Sol","Sol","Sol","Sol",
                    "Sol","Sol","Sol","Sol",
                    "Re","Mi","Mi","Re",
                    // Here a moo there a moo everywhere a moo moo
                    "Si","Si","La","La","Sol"
                ),
                description = "Farm animal song 🐄"
            ),

            Song(
                id = "abc_song",
                name = "ABC Song",
                notes = listOf(
                    // A B C D E F G
                    "Do","Do","Sol","Sol","La","La","Sol",
                    // H I J K L M N O P
                    "Fa","Fa","Mi","Mi","Re","Re","Do",
                    // Q R S  /  T U V
                    "Sol","Sol","Fa","Fa","Mi","Mi","Re",
                    "Sol","Sol","Fa","Fa","Mi","Mi","Re",
                    // W X Y and Z
                    "Do","Do","Sol","Sol","La","La","Sol",
                    // Now I know my ABCs, next time won't you sing with me
                    "Fa","Fa","Mi","Mi","Re","Re","Do"
                ),
                description = "Learn the alphabet! 🔤"
            ),

            Song(
                id = "johny_johny",
                name = "Johny Johny Yes Papa",
                notes = listOf(
                    // Johny Johny Yes Papa
                    "Do","Do","Sol","Sol","La","La","Sol",
                    // Eating sugar No Papa
                    "Fa","Fa","Mi","Mi","Re","Re","Do",
                    // Telling lies No Papa
                    "Sol","Sol","Fa","Mi","Mi","Re",
                    // Open your mouth Ha ha ha
                    "Sol","Sol","Sol","Fa","Sol","Si","Do"
                ),
                description = "Viral nursery rhyme 👦"
            ),

            Song(
                id = "baa_baa_sheep",
                name = "Baa Baa Black Sheep",
                notes = listOf(
                    // Baa baa black sheep have you any wool
                    "Do","Do","Sol","Sol","La","La","La","La","Sol",
                    "Fa","Fa","Mi","Mi","Re","Re","Do",
                    // Yes sir yes sir three bags full
                    "Sol","Sol","Sol","Fa","Fa","Mi","Mi","Mi","Re",
                    // One for the master one for the dame
                    "Sol","Sol","Sol","Fa","Fa","Fa","Fa","Mi","Mi","Mi","Re",
                    // One for the little boy who lives down the lane
                    "Do","Do","Sol","Sol","La","La","La","La","Sol",
                    "Fa","Fa","Mi","Mi","Re","Re","Do"
                ),
                description = "Classic nursery rhyme 🐑"
            ),

            Song(
                id = "mary_lamb",
                name = "Mary Had a Little Lamb",
                notes = listOf(
                    // Mary had a little lamb, little lamb, little lamb
                    "Mi","Re","Do","Re","Mi","Mi","Mi",
                    "Re","Re","Re","Mi","Sol","Sol",
                    // Mary had a little lamb its fleece was white as snow
                    "Mi","Re","Do","Re","Mi","Mi","Mi",
                    "Mi","Re","Re","Mi","Re","Do"
                ),
                description = "Classic nursery rhyme 🐑"
            ),

            Song(
                id = "hot_cross_buns",
                name = "Hot Cross Buns",
                notes = listOf(
                    // Hot cross buns (x2)
                    "Mi","Re","Do",
                    "Mi","Re","Do",
                    // One a penny two a penny
                    "Do","Do","Do","Do",
                    "Re","Re","Re","Re",
                    // Hot cross buns
                    "Mi","Re","Do"
                ),
                description = "Simple and sweet 🍞"
            ),

            Song(
                id = "row_row_boat",
                name = "Row Row Row Your Boat",
                notes = listOf(
                    // Row row row your boat gently down the stream
                    "Do","Do","Do","Re","Mi",
                    "Mi","Re","Mi","Fa","Sol",
                    // Merrily merrily merrily merrily life is but a dream
                    "Do2","Do2","Do2",
                    "Sol","Sol","Sol",
                    "Mi","Mi","Mi",
                    "Do","Do","Do",
                    "Sol","Fa","Mi","Re","Do"
                ),
                description = "Relaxing boat song 🚣"
            ),


            // ==================== MEDIUM (10 bài) ====================



            Song(
                id = "head_shoulders",
                name = "Head Shoulders Knees and Toes",
                notes = listOf(
                    // Head shoulders knees and toes, knees and toes
                    "Do","Mi","Sol","Sol","La","Sol","Fa","Mi",
                    "Re","Mi","Fa","Mi",
                    // (repeat)
                    "Do","Mi","Sol","Sol","La","Sol","Fa","Mi",
                    "Re","Mi","Fa","Mi",
                    // Eyes and ears and mouth and nose
                    "Sol","Sol","La","La","Sol","Sol","Fa","Mi",
                    // Head shoulders knees and toes, knees and toes
                    "Do","Mi","Sol","Sol","La","Sol","Fa","Mi",
                    "Re","Mi","Fa","Mi","Re","Do"
                ),
                description = "Body parts exercise song 🙆"
            ),

            Song(
                id = "if_youre_happy",
                name = "If You're Happy and You Know It",
                notes = listOf(
                    // If you're happy and you know it clap your hands
                    "Do","Do","Fa","Fa","Fa","Fa","Fa","Fa","Mi","Fa","Sol",
                    // (repeat)
                    "Do","Do","Sol","Sol","Sol","Sol","Sol","Sol","Fa","Sol","La",
                    // If you're happy and you know it then your face will surely show it
                    "La","La","Si","Si","Si","Si","Re","Re","Si","Si","La",
                    "La","La","Sol","Fa","Fa","La","La","Sol","Sol","Sol","Fa","Mi","Mi","Re","Mi","Fa",
                    // If you're happy and you know it clap your hands
                    "Do","Do","Fa","Fa","Fa","Fa","Fa","Fa","Mi","Fa","Sol"
                ),
                description = "Interactive fun song 👏"
            ),

            Song(
                id = "five_little_ducks",
                name = "Five Little Ducks",
                notes = listOf(
                    // Five little ducks went out one day
                    "Sol","Sol","La","Sol","Mi","Sol","La","Sol",
                    // Over the hill and far away
                    "Sol","La","Do2","Si","La","Sol","Fa","Mi","Re","Do",
                    // Mother duck said quack quack quack quack
                    "Do","Mi","Sol","Sol","Sol","La","Sol",
                    // But only four little ducks came back
                    "Fa","Mi","Re","Do","Re","Mi","Do",
                    // (verse 2: four little ducks)
                    "Sol","Sol","La","Sol","Mi","Sol","La","Sol",
                    "Sol","La","Do2","Si","La","Sol","Fa","Mi","Re","Do",
                    "Do","Mi","Sol","Sol","Sol","La","Sol",
                    "Fa","Mi","Re","Do","Re","Mi","Do"
                ),
                description = "Counting ducks song 🦆"
            ),

            Song(
                id = "happy_birthday",
                name = "Happy Birthday to You",
                notes = listOf(
                    // Happy birthday to you (x2)
                    "Do","Do","Re","Do","Fa","Mi",
                    "Do","Do","Re","Do","Sol","Fa",
                    // Happy birthday dear [name]
                    "Do","Do","Do2","La","Fa","Mi","Re",
                    // Happy birthday to you
                    "Si","Si","La","Fa","Sol","Fa"
                ),
                description = "Birthday celebration song 🎂"
            ),

            Song(
                id = "jingle_bells",
                name = "Jingle Bells",
                notes = listOf(
                    // Dashing through the snow...
                    "Mi","Mi","Mi","Mi","Mi","Mi","Mi","Sol","Do","Re","Mi",
                    // O'er the fields we go laughing all the way
                    "Fa","Fa","Fa","Fa","Fa","Mi","Mi","Mi",
                    "Mi","Re","Re","Mi","Re","Sol",
                    // Bells on bobtail ring...
                    "Mi","Mi","Mi","Mi","Mi","Mi","Mi","Sol","Do","Re","Mi",
                    // What fun it is to ride...
                    "Fa","Fa","Fa","Fa","Fa","Mi","Mi","Mi",
                    "Sol","Sol","Fa","Re","Do",
                    // Chorus: Jingle bells jingle bells jingle all the way (x2)
                    "Mi","Mi","Mi","Mi","Mi","Mi",
                    "Mi","Sol","Do","Re","Mi",
                    "Fa","Fa","Fa","Fa","Fa","Mi","Mi","Mi",
                    "Mi","Re","Re","Mi","Re","Sol",
                    "Mi","Mi","Mi","Mi","Mi","Mi",
                    "Mi","Sol","Do","Re","Mi",
                    "Fa","Fa","Fa","Fa","Fa","Mi","Mi","Mi",
                    "Sol","Sol","Fa","Re","Do"
                ),
                description = "Christmas classic 🎅"
            ),

            Song(
                id = "hickory_dickory",
                name = "Hickory Dickory Dock",
                notes = listOf(
                    // Hickory dickory dock
                    "Mi","Fa","Sol","Fa","Mi","Re","Mi",
                    // The mouse ran up the clock
                    "Mi","Mi","Sol","Fa","Re","Mi",
                    // The clock struck one
                    "Mi","Mi","Mi","Sol","Fa","Fa",
                    // The mouse ran down
                    "La","Sol","La","Sol","Fa","Mi","Re",
                    // Hickory dickory dock
                    "Do","Do"
                ),
                description = "Hickory dickory dock 🕰️"
            ),

            Song(
                id = "humpty_dumpty",
                name = "Humpty Dumpty",
                notes = listOf(
                    // Humpty Dumpty sat on a wall
                    "Mi","Sol","Fa","La","Sol","La","Si","Do2",
                    // Humpty Dumpty had a great fall
                    "Mi","Sol","Fa","La","Sol","Fa","Mi","Re",
                    // All the king's horses and all the king's men
                    "Mi","Mi","Sol","Fa","Fa","La","Sol","La","Si","Do",
                    // Couldn't put Humpty together again
                    "Do","Mi","Mi","Do"
                ),
                description = "Classic nursery rhyme 🥚"
            ),// m, s, f, l, s, l, si, do2, mi, sol,fa, la,sol,fa,m,r, m,m,s,f,f,l,s,l,si,do,d,m,m,d


            // ==================== HARD (10 bài) ====================

            Song(
                id = "ode_to_joy",
                name = "Ode to Joy",
                notes = listOf(
                    // Phrase A
                    "Mi","Mi","Fa","Sol","Sol","Fa","Mi","Re",
                    "Do","Do","Re","Mi","Mi","Re","Re",
                    // Phrase A (repeat)
                    "Mi","Mi","Fa","Sol","Sol","Fa","Mi","Re",
                    "Do","Do","Re","Mi","Re","Do","Do",
                    // Bridge
                    "Re","Re","Mi","Do","Re","Mi","Fa","Mi","Do",
                    "Re","Mi","Fa","Mi","Re","Do","Re","Sol",
                    // Final phrase
                    "Mi","Mi","Fa","Sol","Sol","Fa","Mi","Re",
                    "Do","Do","Re","Mi","Re","Do","Do"
                ),
                description = "Beethoven's masterpiece 🎵"
            ),

            Song(
                id = "frere_jacques",
                name = "Frère Jacques",
                notes = listOf(
                    // Frère Jacques (x2)
                    "Do","Re","Mi","Do",
                    "Do","Re","Mi","Do",
                    // Dormez-vous? (x2)
                    "Mi","Fa","Sol",
                    "Mi","Fa","Sol",
                    // Sonnez les matines! (x2)
                    "Sol","La","Sol","Fa","Mi","Do",
                    "Sol","La","Sol","Fa","Mi","Do",
                    // Din din don (x2)
                    "Do","Sol","Do",
                    "Do","Sol","Do"
                ),
                description = "French folk song 🔔"
            ),

            Song(
                id = "london_bridge",
                name = "London Bridge Is Falling Down",
                notes = listOf(
                    // London bridge is falling down, falling down, falling down
                    "Sol","La","Sol","Fa","Mi","Fa","Sol",
                    "Re","Mi","Fa","Mi","Fa","Sol",
                    // London bridge is falling down my fair lady
                    "Sol","La","Sol","Fa","Mi","Fa","Sol",
                    "Re","Sol","Mi","Do"
                ),
                description = "Traditional nursery rhyme 🌉"
            ),

            Song(
                id = "when_the_saints",
                name = "When the Saints Go Marching In",
                notes = listOf(
                    // Oh when the saints
                    "Do","Mi","Fa","Sol",
                    // Go marching in
                    "Do","Mi","Fa","Sol",
                    // Oh Lord I want to be in that number
                    "Do","Mi","Fa","Sol","Mi","Do","Mi","Re",
                    "Mi","Mi","Re","Do",
                    // When the saints go marching in
                    "Do","Mi","Sol","Sol","Sol","Fa","Mi","Fa","Sol",
                    "Mi","Do","Re","Do"
                ),
                description = "Famous gospel march 🎺"
            ),

            Song(
                id = "you_are_sunshine",
                name = "You Are My Sunshine",
                notes = listOf(
                    // Verse 1
                    "Do","Do","Re","Mi","Mi","Mi","Re","Mi","Do",
                    "Do","Do","Re","Mi","Fa","La","La","Sol","Fa","Mi","Do",
                    "Re","Mi","Fa","La","La","Sol","Fa","Mi","Do",
                    "Do","Re","Mi","Fa","Re","Re","Mi","Do",
                    // Verse 2
                    "Do","Do","Re","Mi","Mi","Mi","Re","Mi","Do",
                    "Do","Do","Re","Mi","Fa","La","La","Sol","Fa","Mi","Do",
                    "Re","Mi","Fa","La","La","Sol","Fa","Mi","Do",
                    "Do","Re","Mi","Fa","Re","Re","Mi","Do",
                    // Verse 3
                    "Do","Do","Re","Mi","Mi","Mi","Re","Mi","Do",
                    "Do","Do","Re","Mi","Fa","La","La","Sol","Fa","Mi","Do",
                    "Re","Mi","Fa","La","La","Sol","Fa","Mi","Do",
                    "Do","Re","Mi","Fa","Re","Re","Mi","Do"
                ),
                description = "Classic sunshine song ☀️"
            ),


            Song(
                id = "this_old_man",
                name = "This Old Man (Nick Nack Paddywhack)",
                notes = listOf(
                    // This old man he played one, he played nick nack on my drum
                    "Sol","Mi","Sol","Sol","Mi","Sol",
                    "La","Sol","Fa","Mi","Re","Mi",
                    // With a nick nack paddywhack give a dog a bone
                    "Fa","Mi","Fa","Sol","Do","Do","Do","Do","Do","Re","Mi",
                    // This old man came rolling home
                    "Fa","Sol","Sol","Re","Re","Fa","Mi",
                    // Verse 2: he played two, on my shoe
                    "Re","Do"
                ),
                description = "Counting rhyme song 👴"
            ),


        )
    }

    fun getSongById(id: String): Song? {
        return getallSongs().find { it.id == id }
    }


}
