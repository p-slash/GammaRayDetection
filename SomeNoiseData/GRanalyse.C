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

void histogramFill (int *Data, int Len) {
    int k, i;
    int entries = 0;
    const char *colors[] = {"Red", "Green", "Blue", "All"};
    char buffer[30];
    TH1I* hists[4];

    // Start by setting ROOT histogram drawing options
    //gStyle->SetOptFit(1111); // show fit parameters when drawing histograms
	
    // Initialize the histograms
    for (k = 0; k < 4; k++) {
        sprintf(buffer, "Noise of %s Pixels", colors[k]);
	    hists[k] = new TH1I(colors[k], buffer, MAX_PIX_VALUE + 1, 0, MAX_PIX_VALUE + 1);    
    }

    for (k = 1; k < Len; k++) {    
        i = k / (MAX_PIX_VALUE + 1);
        entries += Data[k];
        hists[i]->AddBinContent( k - (i * (MAX_PIX_VALUE + 1)), Data[k]);
        hists[3]->AddBinContent( k - (i * (MAX_PIX_VALUE + 1)), Data[k]);
    }

    hists[3]->setEntries(entries);
    hists[3]->Draw();
    sprintf(buffer, "%s.pdf", colors[k]);  
    gPad->SaveAs(buffer);
    
    entries /= 3;

    for (k = 0; k < 3; k++) {
        hists[k]->setEntries(entries);
        hists[k]->Draw();
        sprintf(buffer, "%s.pdf", colors[k]);  
        gPad->SaveAs(buffer);
    }
    


}

#if !defined(__CINT__) || defined(__MAKECINT__)
int main() {
	FILE * toRead;
	int *NPixData, temp, i, j; 	

	//Open the file for reading:
    toRead = fopen("pixelData.txt", "r");

	if (toRead == NULL) {
       printf("Can't open the file:( \n");
       
       return 0;
    }

    fscanf(toRead, "%d\n%d", &MAX_PIX_VALUE, &PIC_NUMBER);

    DataLength 	= (MAX_PIX_VALUE + 1) * 3;

    NPixData 	= (int*)malloc(DataLength*sizeof(int));

    if(NPixData == NULL) {
        cout << "Mem error\n" << endl;
        return 0;
    }

    i = 0;
    while (fscanf(toRead, "%d\n", &temp) != EOF)
    	NPixData[i++] = temp;

    fclose(toRead);
    
    histogramFill(NPixData, DataLength);
    
    delete[] NPixData;

    NPixData = NULL;

  	return 0;
}
#endif