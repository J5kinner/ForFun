const mDiff = (n, arr) => {
    let totals = 0;
    let sorted = arr.sort();
    for(let i=0; i<n-1; i++){
        totals += sorted[i+1] - sorted[i];        
    }
    return totals;
}

const total = minDiff(5, [1, 3, 3, 2, 4])
console.log(total);