#include <stdlib.h>
#include <iostream>
#include <string.h>
#include "TH1F.h"
#include "TCanvas.h"  // gPad defined here
#include "TStyle.h" // gStyle defined here

using namespace std;

const int MAX_PIX_VALUE = 50;
const int DATA_LENGTH   = 153;
const char* COLORS[] = {"Red", "Green", "Blue", "All"};

int PIC_NUMBER    = 0;

int ReadData(const char* fname, int* NData) {
    FILE* toRead;
    int temp, i;

    toRead = fopen(fname, "r");

    if (toRead == NULL) {
       printf("Can't open the file:( \n");
       
       return 0;
    }

    fscanf(toRead, "%d\n%d\n", &temp, &PIC_NUMBER);

    if (temp != MAX_PIX_VALUE) {
        cout << "Incompatible MAX_PIX_VALUE\n";
        return 0;
    }

    i = 0;
    while (fscanf(toRead, "%d\n", &temp) != EOF)
        NData[i++] = temp;

    fclose(toRead);

    return 1;
}

int WriteData(const char* fname, int* NData) {
	FILE* toWrite;
	int temp, i;
	
	toWrite =fopen(fname, "w");

	if (toWrite == NULL)
	{
		printf("Can't open the file\n");

		return 0;
	}

	fprintf(toWrite, "%d\n%d\n", MAX_PIX_VALUE, PIC_NUMBER);

	i = 0;
	while (i < DATA_LENGTH) {
		fprintf(toWrite, "%d\n", NData[i]);
		i++;
	}

	fclose(toWrite);

	return 1;
}

int main() {
	int *NPixData1, *NPixData2;
	int *result;
	
    NPixData1    = (int*)malloc(DATA_LENGTH*sizeof(int));
    NPixData2    = (int*)malloc(DATA_LENGTH*sizeof(int));
    result 		 = (int*)malloc(DATA_LENGTH*sizeof(int));

    if (NPixData1 == NULL || NPixData2 == NULL) {
        cout << "Mem error\n" << endl;
        return 0;
    }

    if (!ReadData("NoisePixelData.txt", NPixData1))
        return 0;

    if (!ReadData("GammaPixelData.txt", NPixData2))
        return 0;

    for (int i = 0; i < DATA_LENGTH; i++)
    {
    	result[i] = NPixData2[i] - NPixData1[i];
    	cout << result[i] << endl;
    }

    WriteData("Differs.txt", result);

    delete[] NPixData1;
    delete[] NPixData2;
    delete[] result;

    NPixData1 = NULL;
    NPixData2 = NULL;
    result = NULL;
  	return 0;
}