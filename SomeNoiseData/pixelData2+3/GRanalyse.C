#include <stdlib.h>
#include <iostream>
#include <string.h>
#include "TH1F.h"
#include "TCanvas.h"  // gPad defined here
#include "TStyle.h" // gStyle defined here

using namespace std;
int PIC_NUMBER    = 0;
int MAX_PIX_VALUE = 0;
int DataLength    = 0;

void histogramFillAll (long int *Data) {
    int k, i;
    //int entries = 0;
    const char* colors[] = {"Red", "Green", "Blue", "All"};
    char buffer[30];
    TH1I* hists[4];

    // Start by setting ROOT histogram drawing options
    gStyle->SetOptStat(111110);  // show under/overflow when drawing histograms
    gStyle->SetOptFit(1111); // show fit parameters when drawing histograms
	
    // Initialize the histograms
    for (k = 0; k < 4; k++) {
        sprintf(buffer, "Noise of %s Pixels", colors[k]);
	    hists[k] = new TH1I(colors[k], buffer, MAX_PIX_VALUE + 1, 0, MAX_PIX_VALUE + 1);    
    }

    for (k = 0; k < DataLength; k++) {    
        i = k / (MAX_PIX_VALUE + 1);
        //entries += Data[k];
        hists[i]->SetBinContent((k + 1) - (i * (MAX_PIX_VALUE + 1)), Data[k]);
        hists[3]->SetBinContent((k + 1) - (i * (MAX_PIX_VALUE + 1)), Data[k]);
    }

    for (k = 0; k < 4; k++) {
        //hists[k]->setEntries(entries);
        hists[k]->Draw();
        sprintf(buffer, "%s.pdf", colors[k]);  
        gPad->SaveAs(buffer);

        hists[k]->~TH1I();
    }
}

void histogramFillHigh(long int *Data, int offset) {
    const char* colors[] = {"Red", "Green", "Blue", "All"};
    int k, i;
    char buffer[60];
    TH1I* hists[4];

    // Start by setting ROOT histogram drawing options
    gStyle->SetOptStat(111110);  // show under/overflow when drawing histograms
    gStyle->SetOptFit(1111); // show fit parameters when drawing histograms
    
    // Initialize the histograms
    for (k = 0; k < 4; k++) {
        sprintf(buffer, "Noise of %s Pixels Start Value: %d", colors[k], offset);
        hists[k] = new TH1I(colors[k], buffer, MAX_PIX_VALUE - offset + 1, offset, MAX_PIX_VALUE + 1);    
    }

    for (k = offset; k < DataLength; k++) {    
        i = k / (MAX_PIX_VALUE + 1);
        
        if (((k - offset) - (i * (MAX_PIX_VALUE + 1))) < 0)
            k += offset;

        hists[i]->SetBinContent((k + 1 - offset) - (i * (MAX_PIX_VALUE + 1)), Data[k]);
        hists[3]->SetBinContent((k + 1 - offset) - (i * (MAX_PIX_VALUE + 1)), Data[k]);
    }

    for (k = 0; k < 4; k++) {
        hists[k]->Draw();
        sprintf(buffer, "%s_%d.pdf", colors[k], offset);  
        gPad->SaveAs(buffer);

        hists[k]->~TH1I();
    }
}

int ReadData(const char* fname, int *aData) {
    FILE* toRead;
    int temp, i, j;

    //Open the file for reading:
    toRead = fopen(fname, "r");

    if (toRead == NULL) {
       printf("Can't open the file:( \n");
       
       return 0;
    }

    fscanf(toRead, "%d\n%d\n", &MAX_PIX_VALUE, &PIC_NUMBER);

    DataLength  = (MAX_PIX_VALUE + 1) * 3;

    aData    = (int*)malloc(DataLength*sizeof(int));

    if(aData == NULL) {
        cout << "Mem error\n" << endl;
        return 0;
    }

    i = 0;
    while (fscanf(toRead, "%d\n", &temp) != EOF)
        aData[i++] = temp;

    fclose(toRead);

    return 1;
}

int combineTwoFiles(const char* fname1, const char* fname2, long int *NData) {
    int *n1Data, *n2Data;
    int i = 0;


    if (!ReadData(fname1, n1Data))
        return 0;
    
    if (!ReadData(fname2, n2Data))
        return 0;
   
    NData    = (long int*)malloc(DataLength*sizeof(long int));
   
    if(NData == NULL)
        return 0;

    for(i = 0; i < DataLength; i++) {
    	cout << n2Data[i]<< endl;
        NData[i]    = n1Data[i] + n2Data[i];
        
    }

    return 1;
}

#if !defined(__CINT__) || defined(__MAKECINT__)
int main() {
	long int *NPixData;
		
    if(!combineTwoFiles("E_pixelData2.txt", "E_pixelData3.txt", NPixData))
    	return 0;	
    
    histogramFillAll(NPixData);
    histogramFillHigh(NPixData, 20);
    histogramFillHigh(NPixData, 25);
    histogramFillHigh(NPixData, 30);

    delete[] NPixData;

    NPixData = NULL;

  	return 0;
}
#endif