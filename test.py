# Takes facebook messenger links from args or stdin

import sys
from fbchat import Client
from fbchat.models import *
import random


# Usage
def usage():
    print("This is a tool used to generate secret santas over Facebook Messenger.")
    print("Usage:\t\tpython secret-santa.py [-options] [<input-file>]")
    print("Options:")
    print("-h\t\tHelp: display this message.")
    print("-t\t\tTest: print out the sorting order instead of sending messages.")
    print("-u \t\tUser: The host's username and password are the first two lines of the input file.")
    print("\t\tIf this option is not set, you will be prompted for login details.")
    print("\nThe input file should contain a list of participants' Messenger account links, each on a new line.")
    print("messenger.com/t/<username>")
    exit(0)


if len(sys.argv) == 1 or sys.argv[1] == "-h":
    usage()

del sys.argv[0]
userInFile = "-u" in sys.argv
testMode = "-t" in sys.argv

# this jank to remove options from the arguments
k = len(sys.argv)
for i in range(k):
    j = k - i - 1;

    string = sys.argv[j]
    if string[0] == '-':
        del sys.argv[j]

# Open input file
if len(sys.argv) != 0:
    filename = sys.argv[0]
else:
    filename = input("Input file > ")

openSuccess = False
while not openSuccess:
    try:
        file = open(sys.argv[0], "r")
    except:
        print("Input file could not be opened.")
        filename = input("Input file > ")
    else:
        openSuccess = True
        print("Opened file successfully.")

# get login details
if userInFile:
    username = file.readline().rstrip('\n')
    password = file.readline().rstrip('\n')
else:
    username = input("Host email > ")
    password = input("Host password > ")

# login to facebook here
if not testMode:
    loginSuccess = False
    while not loginSuccess:
        try:
            client = Client(username, password, None, 1)
        except FBchatException:
            print("Login failed...")
            username = input("Host email > ")
            password = input("Host password > ")
        except:
            print("There has been an error with the client login system...")
            break
        else:
            print("Login successful!")
            loginSuccess = True

# make collection of all users in the file
inputParticipants = file.read().splitlines()

# sort the list randomly and store the head of the list
# each participant is assigned the next member of the list, and the last participant is assigned the first.
print("Count: " + str(len(inputParticipants)) + ", Inputs: ")
for p in inputParticipants:
    p = p.strip()
    # TODO: find a way to get user ID from url
    print(p)    # .name

sortedParticipants = []
while len(inputParticipants) > 0:
    i = random.randrange(len(inputParticipants))
    sortedParticipants.append(inputParticipants.pop(i))

if testMode:
    sortedNames = []

    print("\nOutput: ")
    for p in sortedParticipants:

        print(p)
else:
    # send a message to all participants with the name of the next person in the list
    thread_type = ThreadType.USER
    if len(sortedParticipants) != 0:
        for i in range(len(sortedParticipants)):
            if i == len(sortedParticipants)-1:
                j = 0
            else:
                j = i+1
            thisUID = sortedParticipants[i]
            nextUID = sortedParticipants[j]
            try:
                client.send(Message(text="Hi there! Your Secret Santa is {}!".format(nextUID.name), thread_id=thisUID,thread_type=thread_type))
            except:
                print("Error sending message to {}".format(thisUID.name))

file.close()
