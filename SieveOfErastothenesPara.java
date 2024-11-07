import java.util.Arrays;
import java.util.concurrent.CyclicBarrier;

 class SieveOfErastothenesPara {
    /**
     * Declaring all the global variables
     *
     */
    int n, k, root, numOfPrimes;
    byte[] oddNumbers;
    byte[][] oddNumbersInThreads;
    CyclicBarrier cb;

  
    /**
     * Constructor that initializes the global variables.
     * @param n Prime numbers up until (and including if prime) 'n' is found
     * @param k Number of cores to utilize in the parallel segment
     */
    public SieveOfErastothenesPara(int n, int k) {
      this.n = n;
      this.k = k;
      root = (int) Math.sqrt(n);
      oddNumbers = new byte[(n / 16) + 1];
      oddNumbersInThreads = new byte[k][(n / 16) + 1];
    }


    /**
     * Performs the sieve and collects the primes produced by the sieve.
     * @return An array containing all the primes up to and including 'n'.
     */
    public int[] getPrimes() {
      if (n <= 1) return new int[0];

      cb = new CyclicBarrier(k + 1);
      // Sequentially finding all primes up to root
      int startInd = nextPrime(1);
      traversePartial(startInd);
      
      // Initializing all threads
      for (int i = 0; i < k; i++) {
        new Thread(new Worker(startInd, i)).start();
        startInd = nextPrime(startInd);
      }
      try {
        cb.await();
      } catch (Exception e) {
        return null;
      }

      concatenateArrays();

      return collectPrimes();
    }


    class Worker implements Runnable {
      /**
       * Declaring local variables for each thread
       */

      int start, i;
      int[] primes;

      /**
       * Constructor that initializes local thread variables
       */
      public Worker(int start, int i) {
          this.start = start;
          this.i = i;
        }

        /**
         * Performing Sieve of Erastothenes in parallel
         */
        public void run() {
          int prime = start; 

          // Performing the sieve
          while (prime != -1) {
            traverse(prime);

            // Skips k prime numbers 
            for(int i = 0; i < k; i++) {
              prime = nextPrime(prime);
              if (prime == -1) 
                break;
            }
          }
          
          try {
            cb.await();
          } catch (Exception e) {
            return;
          }
        }


        /**
         * Marks the number 'num' as a composite number (non-prime) in the 
         * thread's row
         * @param num The number to be marked non-prime
         */
        private void mark(int num) {
          int bitIndex = (num % 16) / 2;
          int byteIndex = num / 16;
          oddNumbersInThreads[this.i][byteIndex] |= (1 << bitIndex);
        }


        /**
         * Marks all odd number multiples of 'prime', starting from prime * prime.
         * @param prime The prime used to mark the composite numbers.
         */
        private void traverse(int prime) {  
          for (int i = prime*prime; i <= n; i += prime * 2) {  
              this.mark(i);
          }
        }
    }


    /**
     * Fills array oddNumbers with primes found in all threads.
     * Wipes oddNumbersInThreads to preserve heap space.
     */
    private void concatenateArrays() {
      for (int num = 0; num < oddNumbers.length; num++) 
        for (int thread = 0; thread < k; thread++) 
          oddNumbers[num] |= oddNumbersInThreads[thread][num]; 
        
      oddNumbersInThreads = null;
    }
 

    /**
     * Traverses sequentially from start to root. Marks all primes less than or
     * Equal to root.
     * @param start The lowest prime to start the traversing.
     */
    private void traversePartial(int start) {
      int prime = start;
      mark(1);

      while (prime != -1) {
        traverse(prime, root);
        prime = nextPrime(prime);
      }
    }


    /**
     * Iterates through the array to count the number of primes found,
     * creates an array of that size and populates the new array with the primes.
     * @return An array containing all the primes up to and including 'n'.
     */
    private int[] collectPrimes() {
      numOfPrimes = 1;
  
      for (int i = 3; i <= n; i += 2)
        if (isPrime(i))
          numOfPrimes++;

      int[] primes = new int[numOfPrimes];
 
      primes[0] = 2;
      int j = 1;
  
      for (int i = 3; i <= n; i += 2)
        if (isPrime(i)) {

          primes[j++] = i;
        }
      return primes;
    }


    /**
     * Marks all odd number multiples of 'prime', starting from prime * prime
     * up to a number high.
     * @param prime The prime used to mark the composite numbers.
     * @param high  The max value to mark.
     */
    private void traverse(int prime, int high) {  
      for (int i = prime*prime; i <= high; i += prime * 2) {  
          mark(i);
      }
    }
  

    /**
     * Finds the next prime in the sequence. If there are no more left, it
     * simply returns -1.
     * @param  prev The last prime that has been used to mark all non-primes.
     * @return      The next prime or -1 if there are no more primes.
     */
    private int nextPrime(int prev) {
      for (int i = prev + 2; i <= root; i += 2)
        if (isPrime(i))
          return i;
  
      return -1;
    }
  
  
    /**
     * Checks if a number is a prime number. If 'num' is prime, it returns true.
     * If 'num' is composite, it returns false.
     * @param  num The number to check.
     * @return     A boolean; true if prime, false if not.
     */
    private boolean isPrime(int num) {
      int bitIndex = (num % 16) / 2;
      int byteIndex = num / 16;
  
      return (oddNumbers[byteIndex] & (1 << bitIndex)) == 0;
    }
  

    /**
     * Marks the number 'num' as a composite number (non-prime)
     * @param num The number to be marked non-prime.
     */
    private void mark(int num) {
      int bitIndex = (num % 16) / 2;
      int byteIndex = num / 16;
      oddNumbers[byteIndex] |= (1 << bitIndex);
    }
  

    /**
     * Prints the primes found.
     * @param primes The array containing all the primes.
     */
    static void printPrimes(int[] primes) {
      for (int prime : primes)
        System.out.println(prime);
    }
  

    /**
     * Expects one or two positive integers as an argument.
     * @param args Contains the number up to which we want to find prime numbers
     * and the number of cores to utilize.
     */
    public static void main(String[] args) {
  
      int n;
      int k;
  
      try {
        n = Integer.parseInt(args[0]);
        if (args.length == 1) 
          k = Runtime.getRuntime().availableProcessors();
        else
          k = Integer.parseInt(args[1]);

        if(n <= 0) throw new Exception();
        if(k <= 0) throw new Exception();
      } catch(Exception e) {
        System.out.println("Correct use of program is: " +
        "java SieveOfEratosthenes <n> <k> where <n> and <k> are positive integers.");
        return;
      }
  
      SieveOfErastothenesPara soe = new SieveOfErastothenesPara(n, k);
  
      /**
       * Getting all the primes equal to and below 'n'
       */
      int[] primes = soe.getPrimes();
  
      /**
       * Printing the primes collected
       */
       printPrimes(primes);
    }
  }