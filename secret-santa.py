# Takes facebook messenger links from args or stdin

import sys
from fbchat import Client
from fbchat.models import *
import random


# Usage - invoke with '-h'
def usage():
    print("This is a tool used to generate secret santas over Facebook Messenger.")
    print("Usage:\t\tpython secret-santa.py [-options]")
    print("Options:")
    print("-h\t\tHelp: display this message.")
    print("-s\t\tSelect: choose to include or exclude specific people in the group. \n\t\tYou will be prompted to "
          "choose before messages are sent.")
    print("-t\t\tTest: print out results instead of sending messages to participants.")
    print("\nTo use this program, you will be prompted to log in with a Facebook account. Don't use your main "
          "account if you are participating in the Secret Santa. You will then need to provide a Group Chat ID: "
          "\n\tmessenger.com/t/<this-is-the-group-chat-id>")
    print("This group should have all the participants of the Secret Santa and the host account.")
    print("If the [-t] option has been set, a randomised list of participants will be printed. Otherwise, "
          "each participant will be sent a message with their Secret Santa and the results will not be printed.")
    exit(0)


class MyUser:
    def __init__(self, user_object, enabled):
        self.user = user_object
        self.enabled = enabled


if len(sys.argv) != 1 and sys.argv[1] == "-h":
    usage()

testMode = "-t" in sys.argv
selectMode = "-s" in sys.argv

# login to facebook here
while True:
    try:
        username = input("Host username > ").rstrip()
        password = input("Host password > ").rstrip()
        client = Client(username, password, None, 1)
    except FBchatException:
        print("Login failed...")
    except:
        print("There has been an error with the client login system... \n"
              "Check fbchat/_state.py line 190, replace with \"revision = 1\"")
        exit(1)
    else:
        print("Login successful!")
        break

# get list of user IDs from group thread
while True:
    try:
        groupID = input("Group ID > ").rstrip()
        print("group: {}".format(groupID))
        inputGroup = client.fetchGroupInfo(groupID).get(groupID)

    except FBchatException:
        print("Could not find a group with this ID...")
    # except:
    #   print("There has been an error collecting the group info...")
    else:
        print("\nGroup \"{}\" found!".format(inputGroup.name))
        if selectMode:
            confirmation = input("Select from users in this group? [Y/n] > ")
        else:
            confirmation = input("Send messages to all other users in this group? [Y/n] > ")
        if confirmation.lower()[0] == 'y':
            break

inputParticipantDict = client.fetchUserInfo(*list(inputGroup.participants))
inputParticipants = inputParticipantDict.values()
myUserList = []

# sort the list randomly and store the head of the list
# each participant is assigned the next member of the list, and the last participant is assigned the first.
print("Host excluded: {}".format(client.fetchUserInfo(client.uid).get(client.uid).name))
print("\nTotal people in group: {} ".format(len(inputParticipants)))
count = 0
for user in inputParticipants:
    myUserList.append(MyUser(user, user.uid != client.uid))


# Allow the user to toggle participants
while True:
    for i in range(len(myUserList)):
        if myUserList[i].enabled:
            symbol = '+'
        else:
            symbol = ' '
        print("{}. [{}] {}".format(i, symbol, myUserList[i].user.name))

    action = input("\nSelect a number to toggle, press \'Y\' to continue or \'N\' to cancel > ")
    print("\n")
    if action[0].lower() == 'y':
        break
    elif action[0].lower() == 'n':
        print("Cancelling... No messages have been sent.")
        exit(0)
    else:
        try:
            action = int(action)
            if 0 <= action < len(myUserList):
                myUserList[action].enabled = not myUserList[action].enabled
        except ValueError:
            print("That is not a valid input.")
        finally:
            print("")


# make a list in a random order, only adding enabled users
sortedParticipants = []
while len(myUserList) > 0:
    i = random.randrange(len(myUserList))
    if myUserList[i].enabled:
        sortedParticipants.append(myUserList.pop(i))
    else:
        myUserList.pop(i)


# send a message to all participants with the name of the next person in the list
if len(sortedParticipants) != 0:
    for i in range(len(sortedParticipants)):
        if i == len(sortedParticipants) - 1:
            j = 0
        else:
            j = i + 1
        thisUser = sortedParticipants[i].user
        nextUser = sortedParticipants[j].user
        try:
            if testMode:
                print("{} --> {}".format(thisUser.name, nextUser.name))
            else:
                client.send(Message(text="Hi there! Your Secret Santa is {}!".format(nextUser.name)), thread_id=thisUser.uid,thread_type=ThreadType.USER)
                print("sent text to person {}".format(i))
        except FBchatException:
            print("Error sending message to {}".format(thisUser.name))
if not testMode:
    print("Messages have been sent.")