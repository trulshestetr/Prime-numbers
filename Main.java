import java.util.concurrent.CyclicBarrier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Main {

    /**
     * Declaring all the global variables.
     */
    static boolean runMedian = false;

    static int n, k, runs = 7, factorizations = 100;
    static int[] nSizes = {2000000, 20000000};
    static Long [] numsToFactorize = new Long[factorizations + 1];
    static String[] colNames = {"n", "sequential(ms) ", "parallel(ms) ", "speedup"};

    static CyclicBarrier cb;
    static Map<Long, ArrayList<Integer>> globalFactors = new HashMap<>();

    
    /**
     * Excpects two positive integers as arguments, and an optional flag to run median times.
     * @param args contains the number up to which we want to find prime numbers and how many 
     * cores to utilize
     */
    public static void main(String[] args) {
        try {
            n = Integer.parseInt(args[0]);
            if (args.length == 1) 
                k = Runtime.getRuntime().availableProcessors();
            else
               k = Integer.parseInt(args[1]);
            
            if (args.length == 3 && args[2].equals("-m")) 
                runMedian = true;

            if(n <= 0) throw new Exception();
            if(k <= 0) throw new Exception();
            
        } catch (Exception e) {
            System.out.println("Correct use of the program is: " + 
            "java Oblig3 <n> <k> -m where <n> and <k> are positive integers, " +
            "and -m is an optional flag to run median times");
            return;
        }

        Main main = new Main();

        getNumsToFactorize();
    
        main.runSingleInstance();
        
        if (runMedian) 
            main.runMedianTimes();
        

    }

    /**
     * Printing execution times for a single run of an algorithm.
     * @param times Exectution times of a method
     * @param type  String to represent which algorithm the times belong to 
     */
    public static void printSingleInstance(float[] times, String type) {
        int longest = 0;

        for(String s : colNames)
            if (s.length() > longest)
                longest = s.length();

        for (float i : times) {
            String s = Float.toString(i);
            if (s.length() > longest)
                longest = s.length();
        }
        String format = "%-" + Integer.toString(longest) + "s";
        
        System.out.println(type);
        for (String s : colNames) 
            System.out.printf(format, s);
        System.out.print("\n");

        System.out.printf(format, n);
        for (float i : times)
            System.out.printf(format, i);

        System.out.print("\n\n\n");
    }


    /**
     * Gets the 100 highest numbers less than n*n.
     */
    public static void getNumsToFactorize() {
        Long max = (long) n*n;
        for (int i = 0; i <= factorizations; i++) {
            max--;
            numsToFactorize[i] = max;
        }
    }

    /**
     * Runs Both versions of sieve an factorization 1 time and prints results.
     * @param sequentialSieve Object to perform sequential sieve
     * @param parallelSieve   Object to perform parallel sieve
     */
    public void runSingleInstance() {
        double start, end, sequentialDuration, parallelDuration;
        int[] sequentialPrimes, parallelPrimes;
        float[] runTiming = new float[3];

        Oblig3 sequentialPrecode = new Oblig3(n);
        Oblig3 parallelPrecode = new Oblig3(n);

        SieveOfErastothenes sequentialSieve = new SieveOfErastothenes(n);
        SieveOfErastothenesPara parallelSieve = new SieveOfErastothenesPara(n, k);
        
        // Measuring and storing execution time for each of the algorithms
        start = System.nanoTime();
        sequentialPrimes = sequentialSieve.getPrimes();
        end = System.nanoTime();
        sequentialDuration = (end - start) / 1000000.0;
        
        start = System.nanoTime();
        parallelPrimes = parallelSieve.getPrimes();
        end = System.nanoTime();
        parallelDuration = (end - start) / 1000000.0;
        
        comparePrimes(sequentialPrimes, parallelPrimes);
        
        runTiming[0] = (float) sequentialDuration;
        runTiming[1] = (float) parallelDuration;
        runTiming[2] = (float) (sequentialDuration / parallelDuration);

        printSingleInstance(runTiming, "Single run time of sieve: ");

        start = System.nanoTime();
        sequentialFactorization(sequentialPrimes, sequentialPrecode);
        end = System.nanoTime();
        sequentialDuration = (end - start) / 1000000.0;
        
        start = System.nanoTime();
        parallelFactorization(parallelPrimes, parallelPrecode);
        end = System.nanoTime();
        parallelDuration = (end - start) / 1000000.0;
        
        runTiming[0] = (float) sequentialDuration;
        runTiming[1] = (float) parallelDuration;
        runTiming[2] = (float) (sequentialDuration / parallelDuration);


        sequentialPrecode.writeFactors("sequential");
        parallelPrecode.writeFactors("parallel");

        printSingleInstance(runTiming, "Single run time of factorizations");

    }

    /**
     * Runs each value of n in nSizes 'runs' number of times for sequential and parallel versions of
     * sieveOfErastothenes and factorization. Prints median times for each value of nSizes and speedup.
     */
    public void runMedianTimes() {
        int sizes = nSizes.length;
        double start, end, duration;

        float sequentialMedian, parallelMedian, speedup;

        int[] sequentialPrimes, parallelPrimes;

        float[][] sequentialSieveTimes = new float[sizes][runs];
        float[][] parallelSieveTimes = new float[sizes][runs];
        float[][] sequentialFactorTimes = new float[sizes][runs];
        float[][] parallelFactorTimes = new float[sizes][runs];

        SieveOfErastothenes sequentialSieve;
        SieveOfErastothenesPara parallelSieve;

        Oblig3 sequentialPrecode;
        Oblig3 parallelPrecode;
        
        
        for (int i = 0; i < sizes; i++) {
            System.out.println("Running for n = " + nSizes[i]);
            
            for (int j = 0; j < runs; j++) {
                System.out.print(j + " ");

                sequentialSieve = new SieveOfErastothenes(nSizes[i]);
                parallelSieve = new SieveOfErastothenesPara(nSizes[i], k);

                sequentialPrecode = new Oblig3(nSizes[i]);
                parallelPrecode = new Oblig3(nSizes[i]);


                start = System.nanoTime();
                sequentialPrimes = sequentialSieve.getPrimes();
                end = System.nanoTime();
                duration = (end - start) / 1000000.0;
                sequentialSieveTimes[i][j] = (float) duration;

                start = System.nanoTime();
                parallelPrimes = parallelSieve.getPrimes();
                end = System.nanoTime();
                duration = (end - start) / 1000000.0;
                parallelSieveTimes[i][j] = (float) duration;

                comparePrimes(sequentialPrimes, parallelPrimes);


                start = System.nanoTime();
                sequentialFactorization(sequentialPrimes, sequentialPrecode);
                end = System.nanoTime();
                duration = (end - start) / 1000000.0;
                sequentialFactorTimes[i][j] = (float) duration;

                start = System.nanoTime();
                parallelFactorization(sequentialPrimes, sequentialPrecode);
                end = System.nanoTime();
                duration = (end - start) / 1000000.0;
                parallelFactorTimes[i][j] = (float) duration;
            }
            System.out.println("\n");
        }


        String format = "%-15s"; 
        int median = (int) Math.floor(runs / 2);
        
        // Printing median times of 'runs' number of executions for both sieve versions 
        System.out.println("Median sieve times of " + runs + " runs ");
        for (String s : colNames) 
            System.out.printf(format, s);

        System.out.print("\n");

        for (int i = 0; i < sizes; i++) {
            
            Arrays.sort(sequentialSieveTimes[i]);
            Arrays.sort(parallelSieveTimes[i]);

            sequentialMedian = sequentialSieveTimes[i][median];
            parallelMedian = parallelSieveTimes[i][median];
            speedup = sequentialMedian / parallelMedian;

            System.out.printf(format, nSizes[i]);
            System.out.printf(format, sequentialMedian);
            System.out.printf(format, parallelMedian);
            System.out.printf(format, speedup);

            System.out.print("\n");
        }
        System.out.print("\n\n");

        // Printing median times of 'runs' number of executions for both factorizing versions 
        System.out.println("Median factorization times of " + runs + " runs ");
        for (String s : colNames) 
            System.out.printf(format, s);
            
        System.out.print("\n");

        for (int i = 0; i < sizes; i++) {
        
            Arrays.sort(sequentialFactorTimes[i]);
            Arrays.sort(sequentialFactorTimes[i]);

            sequentialMedian = sequentialFactorTimes[i][median];
            parallelMedian = parallelFactorTimes[i][median];
            speedup = sequentialMedian / parallelMedian;

            System.out.printf(format, nSizes[i]);
            System.out.printf(format, sequentialMedian);
            System.out.printf(format, parallelMedian);
            System.out.printf(format, speedup);

            System.out.print("\n");
        }
        System.out.print("\n\n");
    }

    /**
     * Prime factorizing the 100 numbers less than n*n sequentially. 
     * @param primes  Integer array of primes to use for factorizing
     * @param precode Precode object to use for managing prime factors
     */
    public void sequentialFactorization(int[] primes, Oblig3 precode) {
        long currentNum;
        int i = 0;
        int prime = primes[i];

        for (long number : numsToFactorize) {
            currentNum = number;

            // Finding prime factors and dividing to get remaining value 
            while (prime*prime <= currentNum) {
                if (currentNum % prime == 0) {
                    currentNum /= prime;
                    precode.addFactor(number, prime);
                }
                else {
                    i++;
                    if (i == primes.length) 
                        break;
                    prime = primes[i];
                }   
            }
            // prime is set to the first prime for next number
            i = 0;
            prime = primes[i];
            if (currentNum != 1)
                precode.addFactor(number, currentNum);
        }
    }




    /**
     * Prime factorizing the 100 numbers less than n*n in parallel.
     * @param primes  Integer array of primes to use for factorizing
     * @param precode Precode object to use for managing prime factors
     */
    public void parallelFactorization(int[] primes, Oblig3 precode) {
        cb = new CyclicBarrier(k + 1);
        
        // Setting up all numbers to factorize as keys in hashmap
        for (Long n : numsToFactorize) {
            globalFactors.put(n, new ArrayList<>());
        }
        for (int i = 0; i < k; i++) {
            new Thread(new FactorizationWorker(i, primes)).start();
        }

        try {
            cb.await();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // Adds all prime factors to precode object for all factorized numbers
        for (Long key : globalFactors.keySet()) {
            Long currentNum = key;

            for (int factor : globalFactors.get(key)) {
                precode.addFactor(key, factor);
                currentNum /= factor;
            }
            // If remaining value is not 1, it is another prime
            if (currentNum != 1) {
                precode.addFactor(key, currentNum);
            }
        }
    }





    class FactorizationWorker implements Runnable {
        /**
         * Declaring local variables for each thread
         */
        int threadNum;
        int[] primes;
        ArrayList<Integer> localFactors;
        Map<Long, ArrayList<Integer>> allLocalFactors = new HashMap<>();

        /**
         * Constructor that initializes local thread variables
         */
        public FactorizationWorker(int i, int[] primes) {
            this.threadNum = i;
            this.localFactors = new ArrayList<>();
            this.primes = primes;
        }

        /**
         * Performing prime factorization in in parallel
         */
        public void run() {
            long currentNum;
            int i = threadNum;
            int prime = primes[threadNum];

            for(long number : numsToFactorize) {
                currentNum = number;

                // Finding prime factors and dividing to get remaining value 
                while (prime*prime <= currentNum && i < primes.length) {
                    if (currentNum % prime == 0) {
                        currentNum /= prime;
                        localFactors.add(prime);
                    }
                    else {
                        // Jumps k primes, for even distribution of primes among the threads
                        i += k;
                        if (i >= primes.length) 
                            break;
                    
                        prime = primes[i];
                    }
                }
                // prime is set to the first prime for next number
                i = threadNum;
                prime = primes[i];

                allLocalFactors.put(number, localFactors);
                localFactors = new ArrayList<>();

            }
            // Sends all factors to syncronized method for storing
            updateFactors(allLocalFactors);

            try {
                cb.await();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Synchronized method to retrieve all factors from the threads to a HashMap 
     * @param localFactors HashMap where key is the number that was factorized
     *  and value is the factors found in the thread that called the method.
     */
    public synchronized void updateFactors(Map<Long,ArrayList<Integer>> localFactors) {
        for (long key : globalFactors.keySet()) {
            globalFactors.get(key).addAll(localFactors.get(key));
        }
    }



    /**
     * Compares the length and elements in two arrays. If arrays are not equal, the first
     * values that don't match are printed. If the arrays are equal, nothing is printed.
     * @param arr1 First array to be compared
     * @param arr2 Second array to be compared
     */
    public static void comparePrimes(int[] arr1, int[] arr2) {
        if (arr1.length != arr2.length)  {
            System.out.println("Arrays are not of equal length");
        }

        for (int i = 0; i < arr1.length; i++) {
            if (arr1[i] != arr2[i]) {
                System.out.println("arr1[" + i + "] = " + arr1[i] 
                            + "and arr2[" + i + "] = " + arr2[i]);
                            return;
            }
        }
    }
}












