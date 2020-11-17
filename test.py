# Takes facebook messenger links from args or stdin

import sys
from fbchat import Client
from fbchat.models import *
import random


# Usage
def usage():
    print("This is a tool used to generate secret santas over Facebook Messenger.")
    print("Usage:\t\tpython secret-santa.py [-options]")
    print("Options:")
    print("-h\t\tHelp: display this message.")
    print("-t\t\tTest: print out results instead of sending messages to participants.")
    print("\nTo use this program, you will be prompted to log in with a Facebook account. Don't use your main "
          "account if you are participating in the Secret Santa. You will then need to provide a Group Chat ID: "
          "\n\tmessenger.com/t/<this-is-the-group-chat-id>")
    print("This group should have all the participants of the Secret Santa and the host account.")
    print("If the [-t] option has been set, a randomised list of participants will be printed. Otherwise, "
          "each participant will be sent a message with their Secret Santa.")
    exit(0)


if len(sys.argv) != 1 and sys.argv[1] == "-h":
    usage()

testMode = "-t" in sys.argv


# login to facebook here
while True:
    try:
        username = input("Host email > ")
        password = input("Host password > ")

        client = Client(username, password, None, 1)
    except FBchatException:
        print("Login failed...")
    except:
        print("There has been an error with the client login system...")
        exit(1)
    else:
        print("Login successful!")
        break

# get list of user IDs from group thread
while True:
    try:
        groupID = input("Group ID > ")
        inputGroup = client.fetchGroupInfo(groupID)
    except FBchatException:
        print("Could not find a group with this ID...")
    except:
        print("There has been an error collecting the group info...")
    else:
        print("Group {} found!".format(groupID.name))

inputParticipants = inputGroup.participants
if client.uid in inputParticipants:
    inputParticipants.remove(client.uid)

# sort the list randomly and store the head of the list
# each participant is assigned the next member of the list, and the last participant is assigned the first.
print("Count: " + str(len(inputParticipants)) + ", Inputs: ")
for p in inputParticipants:
    print(p.name)

sortedParticipants = []
while len(inputParticipants) > 0:
    i = random.randrange(len(inputParticipants))
    sortedParticipants.append(inputParticipants.pop(i))

# send a message to all participants with the name of the next person in the list
if len(sortedParticipants) != 0:
    for i in range(len(sortedParticipants)):
        if i == len(sortedParticipants)-1:
            j = 0
        else:
            j = i+1
        thisUID = sortedParticipants[i]
        nextUID = sortedParticipants[j]
        try:

            if testMode:
                print("{} --> {}".format(thisUID.name, nextUID.name))
            else:
                # client.send(Message(text="Hi there! Your Secret Santa is {}!".format(nextUID.name), thread_id=thisUID,thread_type=ThreadType.USER))
                print("sent text to person {}".format(i))
        except:
            print("Error sending message to {}".format(thisUID.name))

