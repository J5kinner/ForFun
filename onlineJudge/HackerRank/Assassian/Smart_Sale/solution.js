const dProducts = (ids, m) => {
  let removeNum = m;
  let total = 0;
  const idMap = new Map();
  for (let p = 0; p < ids.length; p++) {
    if (idMap.has(ids[p])) {
      let val = idMap.get(ids[p]);
      idMap.set(ids[p], val + 1);
    } else {
      idMap.set(ids[p], 1);
      total += 1;
    }
  }
  for (let search = 1; search <= removeNum; search++) {
    for (let [key, value] of idMap.entries()) {
      //need to find the lowest value in the map and remove it
    //   console.log(key + " , " + value);
      if (search === value && removeNum - value >= 0) {
        // console.log(removeNum);
        // console.log("Removing " + key + " , " + value);
        // console.log(search);
        idMap.delete(key);
        total -= 1;
        removeNum -= value;
        // console.log(removeNum);
      } 
    }
  }

//   console.log(idMap);
  return total;
};
console.log(dProducts([1, 1, 1, 2, 2, 3], 2)) //Ans: 2
console.log(dProducts([5, 5, 5, 6, 6, 7, 7, 8, 8, 8, 8, 8, 8, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 13, 13, 13, 13], 4));
//Ans: 5
console.log(dProducts([10, 11, 12, 12, 12], 3)); //Ans: 1
