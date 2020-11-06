#Jonah Skinner Version 1.0
# Pairs people with each other but not really the secret santa I want. 
#A bunch of code I wrote with the help of fbchat.readthedocs.io
#Main purpose is to send secret santa pairs without the person
#who runs the code to know who is getting who
from itertools import islice
from fbchat import Client
from fbchat.models import *
import random
import time
client = Client('emailofbotAccount', 'password')
thread_type = ThreadType.USER

#tuples of messenger account IDs , Names
olivia_id = "100004648574032"
jonah_id = "100005255521706" , "Jonah"
hamish_id = "100000506289191", "Hamish"
jesse_id = "100000763206918", "Jesse"
connorD_id = "100001494927645", "ConnorD"
bailey_id = "100006847812378", "Bailey"
beez_id = "605579574", "Beeez"
connorP_id = "624019890", "ConnorP"
brendan_id = "837735817", "Brendan"


#Randomizes the list (only works if there is an even amount of people)
family = [jonah_id, hamish_id, jesse_id, connorD_id, bailey_id, beez_id, connorP_id, brendan_id ]
pairs = {}

for p in range(len(family) // 2):
    pairs[p+1] = ( family.pop(random.randrange(len(family))), family.pop(random.randrange(len(family))) )

#Sends out messages from client from the first person then the second in the pair
for x in pairs:
    fbId1 = pairs[x][0] #returns the first of the pair tuple info
    id1 = fbId1[0] #Returns first part of tuple (ID)
    name1 = fbId1[1] #Returns 2nd part of tuple (Name)
    fbId2 = pairs[x][1]
    id2 = fbId2[0]
    name2 = fbId2[1]
    home_id = jonah_id[0]
    print(name1 + " gets"+ name2 )
    client.send(Message(text="🖐Hey there, "+ name1 ), thread_id=id1, thread_type=thread_type)
    time.sleep(1)
    client.send(Message(text="Your TRUE krispy kringle is..."), thread_id=id1, thread_type=thread_type)
    time.sleep(5) #Dramatic effect
    client.send(Message(text= name2 + "👍"), thread_id=id1, thread_type=thread_type)
    client.send(Message(text="🖐Hey there, " + name2 + " please ignore the heresy that is the messages above "), thread_id=id2, thread_type=thread_type)
    time.sleep(1)
    client.send(Message(text="Your TRUE krispy kringle is..." ), thread_id=id2, thread_type=thread_type)
    time.sleep(5)
    client.send(Message(text= name1 + "👍"), thread_id=id2, thread_type=thread_type)

client.send(Message(text="All done the messages have been sent"), thread_id=home_id, thread_type=thread_type)
