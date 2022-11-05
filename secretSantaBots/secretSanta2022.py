#Working concept for a secret santa bot
# All you need to do is enter the password
# Then when prompted give the name of the person you want to send the message to
# Then somehow it finds your friend and you type a message.

import fbchat
from getpass import getpass

username = "jonah.lee.skinner@gmail.com"
client = fbchat.Client(username, getpass())
no_of_friends = int(input("Number of friends: "))
for i in range(no_of_friends):
    name = input("Name: ")
    friends = client.searchForUsers(name)  # return a list of names
    friend = friends[0]
    msg = input("Message: ")
    sent = client.sendMessage(msg, thread_id=friend.uid)
    if sent:
        print("Message sent successfully!")