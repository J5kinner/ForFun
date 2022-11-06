# Jonah Skinner Version 2.0
# fbchat.readthedocs.io helped alot here
# Purpose: 
# Uses a FB account which 
from itertools import islice
from fbchat import Client
from fbchat.models import *
import random
import time
client = Client('jonah.lee.skinner@gmail.com', 'ENTERPASSWORDHEREFROMPHONE')
thread_type = ThreadType.USER

# tuples of messenger account IDs , Names
# OLD ACCOUNTS
# hamish_id = "100000506289191", "Hamish"
# jesse_id = "100000763206918", "Jesse"
# connorD_id = "100001494927645", "ConnorD"
# bailey_id = "100006847812378", "Bailey"
# beez_id = "605579574", "Beeez"
# connorP_id = "624019890", "ConnorP"
# brendan_id = "837735817", "Brendan"

# LAST USED ACCOUNTS
olivia_id = "100004648574032", "Olivia"
jonah_id = "100005255521706", "Jonah"
# cousins
zac_id = "100008829609858", "Zac"
eLane_id = "100000346481540", "Emily Lane"
eSavage_id = "100004695120464", "Emily Savage"
nathan_id = "100002484722119", "Nathan"
jason_id = "100000768545155", "Jason"
alannah_id = "100006155319984", "Alannah"


# Randomizes the list (only works if there is an even amount of people)
# family = [jonah_id, hamish_id, jesse_id, connorD_id, bailey_id, beez_id, connorP_id, brendan_id, olivia_id ]
# Cousins
family = [olivia_id, jonah_id, zac_id, eLane_id,
          eSavage_id, nathan_id, jason_id, alannah_id]
christmasGifs = [
    "https://media1.giphy.com/media/FHqFssDjhXWF6mbTXB/giphy.gif?cid=790b7611ebcd6f0ba3eb6796681bb745361427b2c1274ca8&rid=giphy.gif&ct=g",
    "https://media1.giphy.com/media/iWWIFzpnfuuQg/giphy.gif?cid=ecf05e47p9dnyh84192qr9i3n7zgfwrivttafa50kb8srkrm&rid=giphy.gif&ct=g",
    "https://media0.giphy.com/media/iLaEZQub9aNGkU7pOH/giphy.gif?cid=ecf05e47osox5hs5uujaeya7t5eojnsm4inalryqvzsd5gdb&rid=giphy.gif&ct=g",
    "https://media3.giphy.com/media/LIhO05Vq0d9XqDg2tw/giphy.gif?cid=ecf05e4702uiqj1u5qi8v1jac0hf48vtmbkzrfqm6h2voj0x&rid=giphy.gif&ct=g",
    "https://media3.giphy.com/media/l0HlIxJPz7awJUUW4/giphy.gif?cid=ecf05e47ar6onnd0z0y7cks5k9f64vpat0tndasxi21e6y16&rid=giphy.gif&ct=g",
    "https://media4.giphy.com/media/8iYmvVkfXg0qZEZN7G/giphy.gif?cid=ecf05e475gpz76xv71vubjmfh02s34vi4ama8dgy7kkvsto8&rid=giphy.gif&ct=g",
    "https://media3.giphy.com/media/lDKshubmuQ6GC5wgHJ/giphy.gif?cid=ecf05e475ogksjhzkieccaezelqsbe0i5k1enkdq4c1pjlrt&rid=giphy.gif&ct=g",
    "https://media3.giphy.com/media/l0HlPoAExuyv2HSbm/giphy.gif?cid=ecf05e478z28uk0l9aqn8n3zcveuh6pq8ebcgh0avj3p72dn&rid=giphy.gif&ct=g"
]
home_id = jonah_id[0]
gifCounter = 0
# For counting who current pair to daisy chain first pair to the next pair.
pairCounter = 0
pairs = {}

# Family pair randomiser
for p in range(len(family) // 2):
    pairs[p+1] = (family.pop(random.randrange(len(family))),
                  family.pop(random.randrange(len(family))))


# pairs = {1: (["100005255521706" , "Jonah"], ["100004648574032", "Olivia"])}
firstPersonName = pairs[1][0][1]
# Relationships given out in the following order {
# 1: (firstPerson -> SecondPerson), 
# 2: (secondPerson -> Thirdperson, thirdPerson -> fourthPerson), 
# 3: (fourthPerson -> fifthPerson, lastPerson -> firstPerson),)}
for x in pairs:
    fbId1 = pairs[x][0]
    fbId2 = pairs[x][1]
    id1 = fbId1[0]
    id2 = fbId2[0]
    name1 = fbId1[1]
    name2 = fbId2[1]
    # Daisy chain link to next pair first person
    if x+1 > len(pairs):
        print("Reached the end of the list, first person is " + firstPersonName)
        fbNextPairFirstPerson = firstPersonName
    else:
        fbNextPairFirstPerson = pairs[x+1][0][1]

    #print(name1 + " gets "+ name2 + " gets " + fbNextPairFirstPerson )

    client.sendRemoteFiles(
        christmasGifs[gifCounter], message="", thread_id=id1, thread_type=thread_type)
    gifCounter += 1
    client.send(Message(text="👋 Hey there, " + name1),
                thread_id=id1, thread_type=thread_type)
    time.sleep(1)
    client.send(Message(text="Your secret santa is for..."),
                thread_id=id1, thread_type=thread_type)
    time.sleep(5)  # Dramatic effect
    client.send(Message(text=name2 + "🤫"),
                thread_id=id1, thread_type=thread_type)

    client.sendRemoteFiles(
        christmasGifs[gifCounter], message="", thread_id=id2, thread_type=thread_type)
    gifCounter += 1
    client.send(Message(text="👋 Hey there, " + name2),
                thread_id=id2, thread_type=thread_type)
    time.sleep(1)
    client.send(Message(text="Your secret santa is for..."),
                thread_id=id2, thread_type=thread_type)
    time.sleep(5)  # Dramatic effect
    client.send(Message(text=fbNextPairFirstPerson + "🤫"),
                thread_id=id2, thread_type=thread_type)
client.send(Message(text="My purpose has been served"),
            thread_id=home_id, thread_type=thread_type)
