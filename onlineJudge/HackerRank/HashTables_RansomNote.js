"use strict";

process.stdin.resume();
process.stdin.setEncoding("utf-8");

let inputString = "";
let currentLine = 0;

process.stdin.on("data", function (inputStdin) {
  inputString += inputStdin;
});

process.stdin.on("end", function () {
  inputString = inputString.split("\n");

  main();
});

function readLine() {
  return inputString[currentLine++];
}

/*
 * Complete the 'checkMagazine' function below.
 *
 * The function accepts following parameters:
 *  1. STRING_ARRAY magazine
 *  2. STRING_ARRAY note
 */
let bool = true;
function checkMagazine(magazine, note) {
  // Write your code here
  //magazine: dictionary of words we can use
  //note: what we are checking against to make sure it is correct (case sensitive)

  //Split each line by spaces and fill hash maps for both
  //compare magHash with noteHash
  //if theres a new word in note (case sensitive words included) -> NO
  //if every word in note exists in mag -> YES

  const ht = new HashTable();
  for (const word of magazine) {
    ht.add(word, word);
  }
  for (const term of note) {
    ht.search(term);
  }
  if (bool === true) {
    console.log(ht);

    return console.log("Yes");
  } else {
    console.log(ht);

    return console.log("No");
  }
}

class HashTable {
  //grab our magazine and turn it into a hash map
  constructor() {
    this.values = {};
    this.length = 0;
    this.size = 0;
  }
  calculateHash(key) {
    return key.toString().length % this.size;
  }

  add(key, value) {
    const hash = this.calculateHash(key);
    if (!this.values.hasOwnProperty(hash)) {
      this.values[hash] = {};
    }
    if (!this.values[hash].hasOwnProperty(key)) {
      this.length++;
    }
    this.values[hash][key] = value;
  }

  search(key) {
    const hash = this.calculateHash(key);
    if (
      this.values.hasOwnProperty(hash) &&
      this.values[hash].hasOwnProperty(key) &&
      this.values[hash][key] !== undefined
    ) {
      // console.log(this.values[hash][key]);
      //eliminate duplicates
      this.values[hash][key] = undefined;
    } else {
      bool = false;
      console.log("Changed here at: " + this.values[hash][key]);
    }
  }
}

function main() {
  const firstMultipleInput = readLine().replace(/\s+$/g, "").split(" ");

  const m = parseInt(firstMultipleInput[0], 10);

  const n = parseInt(firstMultipleInput[1], 10);

  const magazine = readLine().replace(/\s+$/g, "").split(" ");

  const note = readLine().replace(/\s+$/g, "").split(" ");

  checkMagazine(magazine, note);
}
