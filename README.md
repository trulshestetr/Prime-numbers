# Prime-numbers

All primes up to a given value *n* are calculated using the Sieve of Eratosthenes and prime number factorization is performed on the 100 largest numbers less than n<sup>2</sup>. The calculations are done both sequentially and in parallel. Time and speedup reportings are printed at the end. All prime number factorizations are written to .txt files. This project is the third assignment in the course IN3030 at the University of Oslo

## How to run

Compile and run the project using the following commands.

```
javac *.java
java Main <n> <k> -m
```

- n decides the primes to generate and numbers to factorize. All primes up to n are calculated and the 100 largest integers less than n<sup>2</sup> are prime factorized.
- k sets the number of cores to utilize for the parallelization. The default value is the number of cores available on the machine.
- -m is the flag for testing speedup. If included, run 7 iterations of different values of n and print median times and speedups.

### Usage example

Find all primes less than or equal to 1.000 and factorize all numbers in the interval *[999899, 999999]* using 8 cores.
```
java Main 1000 8 
```

## Implementation
#### Calculating the prime numbers
The Sieve of Eratosthenes (SoE) parallelization is performed using a 2d array of size `n * k`. Each thread iterates over their respective row and marks non-primes. Then, a bit-wise OR-operation on all rows merge the work of each individual thread. The threads are initiated on the index corresponding to their id, and jumps *k* indexes for each prime number check. This balances workload, since more primes occur at lower integers.  

#### Prime number factorizartion
The prime factorization works by each thread running through a local copy of the integers to factorize. Each thread factorizes every number, but only with every *k-th* prime. what primes to use are decided using the thread id. All threads perform partial factorization on all numbers, storing the factors in a local HashMap. The HashMaps are merged after the iteration, allowing for only *k* synchronziations. After all threads are done and all HashMaps are mergeed, factors are sequentially added to the precode file

## Results
Results are all the median time of seven runs for different values of *n*, run on 8 cores.

#### SoE prime calculations

| n            | Sequential (ms) | Parallel (ms) | speedup |
| ------------ | --------------- | ------------- | ------- |
| 2 000 000    | 7,96            | 7,34          | 1,0804  |
| 20 000 000   | 81,58           | 71,40         | 1,1426  |
| 200 000 000  | 1070,84         | 922,37        | 1,1610  |
| 2000 000 000 | 13964,99        | 10616,96      | 1,3153  |

#### Factorization

| n            | Sequential (ms) | Parallel (ms) | speedup |
| ------------ | --------------- | ------------- | ------- |
| 2 000 000    | 81,06           | 35,54         | 2,2806  |
| 20 000 000   | 624,60          | 233,69        | 2,6728  |
| 200 000 000  | 5183,75         | 2289,65       | 2,2640  |
| 2000 000 000 | 45813,96        | 26806,41      | 1,7091  |

#### SoE and factorization combined

| n            | Sequential (ms) | Parallel (ms) | speedup |
| ------------ | --------------- | ------------- | ------- |
| 2 000 000    | 89,01           | 42,88         |  2,0758 |
| 20 000 000   | 706,18          | 305,09        |  2,3147 |
| 200 000 000  | 6254,59         | 3212,02       |  1,9472 |
| 2000 000 000 | 59778,95        | 37423,36      |  1,5974 |
