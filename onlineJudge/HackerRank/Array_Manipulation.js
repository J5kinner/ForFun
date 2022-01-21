"use strict";

const fs = require("fs");

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
 * Complete the 'arrayManipulation' function below.
 *
 * The function is expected to return a LONG_INTEGER.
 * The function accepts following parameters:
 *  1. INTEGER n
 *  2. 2D_INTEGER_ARRAY queries
 */

function arrayManipulation(n, queries) {
  // Write your code here

  //Questions:
  // edge cases: negatives? letters? null values? assume data is always correct?
  // Do we care about performance?

  //track biggest sum
  // 1 2 3 4 5 6 7 8 9 10
  //[3 3 3 3 3 0 0 0 0 0]
  // 0 0 0 7 7 7 7 7 0 0
  // 0 0 0 0 0 1 1 1 1 1
  // Total: 3 3 3 10 10 8 8 8 1 1
  //first iteration: create set with a-b of k
  //then
  // Answer: 10

  const array = []; //stores out key-value pairs
  let biggest = 0; //largest number we are returning

  queries.forEach((query) => { //goes through all the queries in the input 2d array
    const start = query[0]; //start index
    const end = query[1]; //end index
    const qVal = query[2]; //value placed in the range of indexes

    array.push({ //pushes the start key-value pair onto the array
      key: start,
      val: qVal,
    });
    array.push({ //pushes the end key-value pair onto the array
      key: end,
      val: -qVal, //make the value negative to reduce duplicates being multiplied to the largest value.
    }); //we make this negative because when summing we dont want to add up duplicate values
  });

  //sort the key-value pair array

  array.sort((element1, element2) => {
    if (element1.key === element2.key) { //if the elements being compared are on the same index
      return element2.val - element1.val; //sorts by values in descending order
    }
    return element1.key - element2.key; //sorts by key ascending order
  });

  let sum = 0; //calculates any additions
  for (let i = 0; i < array.length; i++) {
    const param = array[i];
    sum += param.val; 

    if (sum > biggest) {
      biggest = sum;
    }
  }
  return biggest;
}

function main() {
  const ws = fs.createWriteStream(process.env.OUTPUT_PATH);

  const firstMultipleInput = readLine().replace(/\s+$/g, "").split(" ");

  const n = parseInt(firstMultipleInput[0], 10);

  const m = parseInt(firstMultipleInput[1], 10);

  let queries = Array(m);

  for (let i = 0; i < m; i++) {
    queries[i] = readLine()
      .replace(/\s+$/g, "")
      .split(" ")
      .map((queriesTemp) => parseInt(queriesTemp, 10));
  }

  const result = arrayManipulation(n, queries);

  ws.write(result + "\n");

  ws.end();
}
