#include <stdlib.h>
#include <iostream>
#include <string.h>
#include "TH1F.h"
#include "TCanvas.h"  // gPad defined here
#include "TStyle.h" // gStyle defined here

using namespace std;

const int MAX_PIX_VALUE = 255;
const int DATA_LENGTH   = 3 * MAX_PIX_VALUE + 3;
const char* COLORS[] = {"Red", "Green", "Blue", "All"};

int PIC_NUMBER    = 0;

void histogramFillAll (int *Data) {
    int k, i;    
    char buffer[30];
    TH1I* hists[4];

    // Start by setting ROOT histogram drawing options
    gStyle->SetOptStat(111110);  // show under/overflow when drawing histograms
    gStyle->SetOptFit(1111); // show fit parameters when drawing histograms
	
    // Initialize the histograms
    for (k = 0; k < 4; k++) {
        sprintf(buffer, "Noise of %s Pixels", COLORS[k]);

	    hists[k] = new TH1I(COLORS[k], buffer, MAX_PIX_VALUE + 1, 0, MAX_PIX_VALUE + 1);    
    }

    for (k = 0; k < DATA_LENGTH; k++) {    
        i = k / (MAX_PIX_VALUE + 1);

        hists[i]->SetBinContent((k + 1) - (i * (MAX_PIX_VALUE + 1)), Data[k]);
        hists[3]->SetBinContent((k + 1) - (i * (MAX_PIX_VALUE + 1)), Data[k]);
    }

    for (k = 0; k < 4; k++) {        
        hists[k]->Draw();
        
        sprintf(buffer, "%s.pdf", COLORS[k]);

        gPad->SetLogy();
        gPad->SaveAs(buffer);

        hists[k]->~TH1I();
    }
}

void histogramFillHigh(int *Data, int offset) {
    int k, i;
    char buffer[60];
    TH1I* hists[4];

    // Start by setting ROOT histogram drawing options
    gStyle->SetOptStat(111110);  // show under/overflow when drawing histograms
    gStyle->SetOptFit(1111); // show fit parameters when drawing histograms
    
    // Initialize the histograms
    for (k = 0; k < 4; k++) {
        sprintf(buffer, "Noise of %s Pixels Start Value: %d", COLORS[k], offset);

        hists[k] = new TH1I(COLORS[k], buffer, MAX_PIX_VALUE - offset + 1, offset, MAX_PIX_VALUE + 1);    
    }

    for (k = offset; k < DATA_LENGTH; k++) {    
        i = k / (MAX_PIX_VALUE + 1);
        
        if (((k - offset) - (i * (MAX_PIX_VALUE + 1))) < 0)
            k += offset;

        hists[i]->SetBinContent((k + 1 - offset) - (i * (MAX_PIX_VALUE + 1)), Data[k]);
        hists[3]->SetBinContent((k + 1 - offset) - (i * (MAX_PIX_VALUE + 1)), Data[k]);
    }

    for (k = 0; k < 4; k++) {
        hists[k]->Draw();

        sprintf(buffer, "%s_%d.pdf", COLORS[k], offset); 

        gPad->SaveAs(buffer);

        hists[k]->~TH1I();
    }
}

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
/*
int combineTwoFiles(const char* fname1, const char* fname2, int* NData) {
    int *n1Data, *n2Data, len, i;
    
    if (!ReadData(fname1, n1Data))
        return 0;
    
    if (!ReadData(fname2, n2Data))
        return 0;

    NData    = (int*)malloc(DATA_LENGTH*sizeof(int));

    if(NData == NULL)
        return 0;

    for(i = 0; i < DATA_LENGTH; i++)
        NData[i]    = n1Data[i] + n2Data[i];

    return 1;
}
*/
#if !defined(__CINT__) || defined(__MAKECINT__)
int main() {
	int* NPixData;
	
    NPixData    = (int*)malloc(DATA_LENGTH*sizeof(int));
    
    if (NPixData == NULL) {
        cout << "Mem error\n" << endl;
        return 0;
    }

    if (!ReadData("pixelData.txt", NPixData))
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