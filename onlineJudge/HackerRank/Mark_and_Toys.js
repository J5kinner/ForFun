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
 * Complete the 'maximumToys' function below.
 *
 * The function is expected to return an INTEGER.
 * The function accepts following parameters:
 *  1. INTEGER_ARRAY prices
 *  2. INTEGER k
 */

function maximumToys(prices, k) {
  // Write your code here

  const sorted = quickSort(prices, 0, prices.length - 1);
  let bank = k;
  let counter = 0;
  for (let x of sorted) {
    if (x < bank && bank >= 0) {
      bank -= x;
      counter++;
    }
  }
  return counter;
}

//recursively sorts the array in a divide and conquer method.
function quickSort(prices, left, right) {
  let index;

  if (prices.length > 1) {
    index = partition(prices, left, right); //Partition being sorted
    if (left < index - 1) { //more items on the left side of the pivot
      quickSort(prices, left, index - 1);
    }
    if (index < right) { //more items on the right side of the pivot
      quickSort(prices, index, right);
    }
  }
  return prices;
}

function partition(prices, left, right) {
  let pivot = prices[Math.floor((left + right) / 2)], 
    l = left,
    r = right;

  while (l <= r) { //once all values are sorted in ascending order
    while (prices[l] < pivot) {
      l++;
    }
    while (prices[r] > pivot) {
      r--;
    }

    if (l <= r) {
      swap(prices, l, r); //swap the values if the left is smaller than the right
      l++; //move onto the next part of the array
      r--;
    }
  }
  return l;
}

function swap(prices, leftIndex, rightIndex) {
  const priceTemp = prices[leftIndex]; 
  prices[leftIndex] = prices[rightIndex];
  prices[rightIndex] = priceTemp;
}

function main() {
  const ws = fs.createWriteStream(process.env.OUTPUT_PATH);

  const firstMultipleInput = readLine().replace(/\s+$/g, "").split(" ");

  const n = parseInt(firstMultipleInput[0], 10);

  const k = parseInt(firstMultipleInput[1], 10);

  const prices = readLine()
    .replace(/\s+$/g, "")
    .split(" ")
    .map((pricesTemp) => parseInt(pricesTemp, 10));

  const result = maximumToys(prices, k);

  ws.write(result + "\n");

  ws.end();
}
