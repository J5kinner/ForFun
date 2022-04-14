const countOccurences = (string, word) => {
  return string.split(word).length - 1;
};

const firstUniqueCharacter = (str) => {
  //Add word to a set ->
  let chkSet = new Set();

  let index = -1;
  //Check if letter is already in set
  const charArr = str.split("");
  let q = new Array();

  for (let i = 0; i < charArr.length; i++) {
    if (!chkSet.has(charArr[i])) {
      chkSet.add(charArr[i]);
      q.push(i);
    }
  }
//   console.log(q);
//   console.log(chkSet);
//   console.log(charArr);
  for (let j = 0; j < q.length; j++) {
    // console.log("before " + q);
    // console.log(charArr.find((element) => element === charArr[q[j]]));
    // console.log(countOccurences(str, charArr[q[j]]));
    // console.log("Letter " + charArr[q[j]]);
    if (countOccurences(str, charArr[q[j]]) > 1) {
    //   console.log("removing " + charArr[q[j]]);
      q.splice(j, 1);
      j -= 1;
    }
    // console.log("after: " + q);
  }

//   console.log("new array is " + q);
if(q[0] >= 0){
  index = q[0] + 1;
  return index;
}
  return index;
};

console.log(firstUniqueCharacter("statistics"));
console.log(firstUniqueCharacter("jonah"));
console.log(firstUniqueCharacter("akuna"));
console.log(
  firstUniqueCharacter(
    "aaaaaakkkkkkkkkssssssssssaaasssssllldddssssaassssdasdasdasdx"
  )
);

console.log(
  firstUniqueCharacter(
    "aaaaaakkkkkkkkkssssssssssaaasxssssllldddssssaassssdasdasdasdx"
  )
);
