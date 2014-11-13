#include <stdlib.h>
#include <iostream>
#include <string.h>
#include "TH1F.h"
#include "TCanvas.h"  // gPad defined here
#include "TStyle.h" // gStyle defined here

using namespace std;
int PIC_NUMBER = 0;
int PIX_NUMBER = 0;
int DataLength = 0;

void histogramFill (int *Data, int Len) {
	int k = 0;
	char buffer[25];

	// Start by setting ROOT histogram drawing options
  	gStyle->SetOptStat(111110);  // show under/overflow when drawing histograms
  	gStyle->SetOptFit(1111); // show fit parameters when drawing histograms
  
  	// Initialize the histograms
    sprintf(buffer, "Noise of %d Pixels", PIX_NUMBER * 4);
  	TH1I *hist_allall = new TH1I("AllPix", buffer, 18, -3, 15);

    sprintf(buffer, "Noise of %d Alpha Pixels", PIX_NUMBER);
    TH1I *hist_allalp = new TH1I("AllAlpha", buffer, 18, -3, 15);
    
    sprintf(buffer, "Noise of %d Red Pixels", PIX_NUMBER);
    TH1I *hist_allred = new TH1I("AllRed", buffer, 18, -3, 15);
    
    sprintf(buffer, "Noise of %d Green Pixel", PIX_NUMBER);
    TH1I *hist_allgre = new TH1I("AllGreen", buffer, 18, -3, 15);
    
    sprintf(buffer, "Noise of %d Blue Pixel", PIX_NUMBER);
    TH1I *hist_allblu = new TH1I("AllBlue", buffer, 18, -3, 15);
    
    sprintf(buffer, "Noise of One Alpha Pixel");
    TH1I *hist_onealp = new TH1I("OneAlpha", buffer, 18, -3, 15);
    
    sprintf(buffer, "Noise of One Red Pixel");
    TH1I *hist_onered = new TH1I("OneRed", buffer, 18, -3, 15);
    
    sprintf(buffer, "Noise of One Blue Pixel");
    TH1I *hist_onegre = new TH1I("OneGreen", buffer, 18, -3, 15);
    
    sprintf(buffer, "Noise of One Blue Pixel");
    TH1I *hist_oneblu = new TH1I("OneBlue", buffer, 18, -3, 15);
    
	while (k < Len) {

		hist_allall->Fill(Data[k]);
        hist_allall->Fill(Data[k+1]);
        hist_allall->Fill(Data[k+2]);
        hist_allall->Fill(Data[k+3]);

        hist_allalp->Fill(Data[k++]);

        hist_allred->Fill(Data[k++]);
        
        hist_allgre->Fill(Data[k++]);
        
        hist_allblu->Fill(Data[k++]);
    }
	
    k = 0;

    while ( k < Len) {
        hist_onealp->Fill(Data[k++]);
        hist_onered->Fill(Data[k++]);
        hist_onegre->Fill(Data[k++]);
        hist_oneblu->Fill(Data[k++]);
        k += 36;
    }
  
    hist_allall->Draw();  
	gPad->SaveAs("AllAllPix.pdf");

    hist_allalp->Draw();
    gPad->SaveAs("AllAlpha.pdf");

    hist_allred->Draw();
    gPad->SaveAs("AllRed.pdf");

    hist_allgre->Draw();
    gPad->SaveAs("AllGreen.pdf");

    hist_allblu->Draw();
    gPad->SaveAs("AllBlue.pdf");

    hist_onealp->Draw();
    gPad->SaveAs("OneAlpha.pdf");
    
    hist_onered->Draw();
    gPad->SaveAs("OneRed.pdf");

    hist_onegre->Draw();
    gPad->SaveAs("OneGreen.pdf");

    hist_oneblu->Draw();
    gPad->SaveAs("OneBlue.pdf");
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

    fscanf(toRead, "%d,%d", &PIC_NUMBER, &PIX_NUMBER);

    DataLength 	= PIX_NUMBER * PIC_NUMBER * 4;

    NPixData 	= (int*)malloc(DataLength*sizeof(int));

    if(NPixData == NULL) {
        cout << "Mem error\n" << endl;
        return 0;
    }

    i = 0;
    while (fscanf(toRead, ",%d", &temp) != EOF)
    	NPixData[i++] = temp;

    fclose(toRead);
    
    histogramFill(NPixData, DataLength);
    
    delete[] NPixData;

    NPixData    = NULL;

  	return 0;
}
#endif