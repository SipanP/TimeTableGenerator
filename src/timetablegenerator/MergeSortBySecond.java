package timetablegenerator;

public class MergeSortBySecond {
    // Merges two subarrays of arr[]. 
    // First subarray is arr[l..m] 
    // Second subarray is arr[m+1..r] 

    void merge(double arr[][], int l, int m, int r) {
        // Find sizes of two subarrays to be merged 
        int n1 = m - l + 1;
        int n2 = r - m;

        //Create temp arrays
        double L[][] = new double[n1][2];
        double R[][] = new double[n2][2];

        //Copy data to temp arrays
        for (int i = 0; i < n1; ++i) {
            L[i][0] = arr[l + i][0];
            L[i][1] = arr[l + i][1];
        }
        for (int j = 0; j < n2; ++j) {
            R[j][0] = arr[m + 1 + j][0];
            R[j][1] = arr[m + 1 + j][1];
        }

        //Merge the temp arrays
        // Initial indexes of first and second subarrays 
        int i = 0, j = 0;

        // Initial index of merged subarry array 
        int k = l;
        while (i < n1 && j < n2) {
            if (L[i][1] >= R[j][1]) {
                arr[k][0] = L[i][0];
                arr[k][1] = L[i][1];
                i++;
            } else {
                arr[k][0] = R[j][0];
                arr[k][1] = R[j][1];
                j++;
            }
            k++;
        }

        // Copy remaining elements of L[]
        while (i < n1) {
            arr[k][0] = L[i][0];
            arr[k][1] = L[i][1];
            i++;
            k++;
        }

        // Copy remaining elements of R[]
        while (j < n2) {
            arr[k][0] = R[j][0];
            arr[k][1] = R[j][1];
            j++;
            k++;
        }
    }

    // Main function that sorts arr[l..r] using merge() 
    void sort(double arr[][], int l, int r) {
        if (l < r) {
            // Find the middle point 
            int m = (l + r) / 2;
            // Sort first and second halves 
            sort(arr, l, m);
            sort(arr, m + 1, r);
            // Merge the sorted halves 
            merge(arr, l, m, r);
        }
    }

    // A utility function to print array of size n
    static void printArray(double arr[][]) {
        int n = arr.length;
        for (int i = 0; i < n; ++i) {
            System.out.print(arr[i][0] + " ");
            System.out.println(arr[i][1] + " ");
        }
        System.out.println();
    }

}
