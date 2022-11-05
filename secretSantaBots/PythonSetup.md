# How to setup Pyenv and Python for your Macbook pro with M1

## Install Homebrew

`brew update`

Install python dependencies: `brew install openssl readline sqlite3 xz zlib tcl-tk`

Install Pyenv: `brew install pyenv`


## For Bash
`echo 'export PYENV_ROOT="$HOME/.pyenv"' >> ~/.bashrc`
`echo 'command -v pyenv >/dev/null || export PATH="$PYENV_ROOT/bin:$PATH"' >> ~/.bashrc`
`echo 'eval "$(pyenv init -)"' >> ~/.bashrc`

## for ZSH
`echo 'export PYENV_ROOT="$HOME/.pyenv"' >> ~/.zshrc`
`echo 'command -v pyenv >/dev/null || export PATH="$PYENV_ROOT/bin:$PATH"' >> ~/.zshrc`
`echo 'eval "$(pyenv init -)"' >> ~/.zshrc`


`exec "$ZSH"`

##    ERROR: revision = int(r.text.split('"client_revision":', 1)[1].split(",", 1)[0])
Change `revision = 1`
inside of `/Users/jonah/.pyenv/versions/3.7.15/lib/python3.7/site-packages/fbchat/_state.py` or where ever it is installed on your machine.

## Side Notes
The reason I made this doc is because the following python will not run on the latest python versions and it is very tedious to find this information and so I'm leaving this here for future you and future me 😄.
