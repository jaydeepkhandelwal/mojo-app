package localhost.mojo;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.List;

/**
 * Created by jaydeep on 31/05/15.
 */
public class APIResponseAdapter extends ArrayAdapter<APIResponseData> {

    private final List<APIResponseData> APIResponseDataList;
    private final Context context;
    private int resource =  R.layout.api_response_item_new;
    public APIResponseAdapter(Context context, int resource, List<APIResponseData> APIResponseDataList) {

        super(context, resource, APIResponseDataList);
        this.resource = resource;
        this.context = context;
        this.APIResponseDataList = APIResponseDataList;

    }
    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        if(view == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            view = inflater.inflate(resource, viewGroup, false);
        }
        TextView carCategoryText = (TextView) view.findViewById(R.id.car_category);
        TextView etaText = (TextView) view.findViewById(R.id.eta);
        TextView estimateText = (TextView) view.findViewById(R.id.estimate);
       // TextView carCategoryTypeText = (TextView)view.findViewById(R.id.car_provider);
        ImageView carCategoryTypeImage = (ImageView) view.findViewById(R.id.car_logo);
        APIResponseData apiResponseData = APIResponseDataList.get(position);
//        if(apiResponseData.getEtaData() == null && apiResponseData.getCarProvider() != null) {
//
//            carCategoryTypeText.setText(apiResponseData.getCarProvider());
//            carCategoryTypeText.setVisibility(View.VISIBLE);
//            etaText.setVisibility(View.GONE);
//            estimateText.setVisibility(View.GONE);
//            carCategoryText.setVisibility(View.GONE);
//        }
//        else {

            if(apiResponseData.getCarProvider() == GlobalVars.OLA){
                carCategoryTypeImage.setImageResource(R.drawable.ola);
            }
            else if(apiResponseData.getCarProvider() == GlobalVars.UBER){
                 carCategoryTypeImage.setImageResource(R.drawable.uber);
             }
            else if(apiResponseData.getCarProvider() == GlobalVars.TFS){
                carCategoryTypeImage.setImageResource(R.drawable.taxiforsure);
            }
            EtaData etaData = apiResponseData.getEtaData();
            PriceData priceData = apiResponseData.getPriceData();

            if (etaData != null) {
                HashMap<String,String> cabNamesMapping = CabNamesMapping.getCabNamesMap();
                String carCategory = etaData.getCarCategory();

                if(cabNamesMapping.containsKey(carCategory)){
                    carCategory = cabNamesMapping.get(carCategory);
                }

                Integer carEta = etaData.getEta();
                String mins = "mins";
                if(carEta == 1)
                    mins = "min";

               String carAndEtaText = carCategory + " is " + etaData.getEta() + " " + mins + " away.";

                etaText.setText(carAndEtaText);
            }
            if(priceData != null) {
                String estimate = priceData.getLowEstimate() + " - " + priceData.getHighEstimate();
                String estimateStr = "Approx fare should be "+ estimate + " Rs.";
                estimateText.setText(estimateStr);
              //  estimateText.setVisibility(View.VISIBLE);
            }
            else {
                estimateText.setText("Fare is not available.");
            }
           // carCategoryTypeText.setVisibility(View.GONE);
           // etaText.setVisibility(View.VISIBLE);
            //carCategoryText.setVisibility(View.VISIBLE);
       // }
        return view;
    }
}

    class APIResponseData{
        private String carProvider;
        private  EtaData etaData;
        private  PriceData priceData;

        public PriceData getPriceData() {
            return priceData;
        }

        public void setPriceData(PriceData priceData) {
            this.priceData = priceData;
        }

        public String getCarProvider() {
            return carProvider;
        }

        public void setCarProvider(String carProvider) {
            this.carProvider = carProvider;
        }

        public EtaData getEtaData() {
            return etaData;
        }

        public void setEtaData(EtaData etaData) {
            this.etaData = etaData;
        }
    }
    class EtaResponse {

        private EtaDataList ola;
        private EtaDataList tfs;
        private EtaDataList uber;
        private Boolean status;

        public EtaDataList getOla() {
            return ola;
        }

        public void setOla(EtaDataList ola) {
            this.ola = ola;
        }

        public EtaDataList getTfs() {
            return tfs;
        }

        public void setTfs(EtaDataList tfs) {
            this.tfs = tfs;
        }

        public Boolean getStatus() {
            return status;
        }

        public void setStatus(Boolean status) {
            this.status = status;
        }

        public EtaDataList getUber() {
            return uber;
        }

        public void setUber(EtaDataList uber) {
            this.uber = uber;
        }
    }


    class EtaDataList {

        private List<EtaData> dataList;
        private Boolean status;

        @JsonProperty("data")
        public List<EtaData> getDataList() {
            return dataList;
        }

        @JsonProperty("data")
        public void setDataList(List<EtaData> dataList) {
            this.dataList = dataList;
        }

        @JsonProperty
        public Boolean getStatus() {
            return status;
        }

        @JsonProperty
        public void setStatus(Boolean status) {
            this.status = status;
        }
    }

    class EtaData {

        private String carCategory;
        private Integer eta;




        @JsonProperty("car_category")
        public String getCarCategory() {
            return carCategory;
        }

        @JsonProperty("car_category")
        public void setCarCategory(String carCategory) {
            this.carCategory = carCategory;
        }

        @JsonProperty
        public Integer getEta() {
            return eta;
        }

        @JsonProperty
        public void setEta(Integer eta) {
            this.eta = eta;
        }

    }

    class PriceResponse {
        private PriceDataList ola;
        private PriceDataList tfs;
        private PriceDataList uber;
        private Boolean status;

        public PriceDataList getOla() {
            return ola;
        }

        public void setOla(PriceDataList ola) {
            this.ola = ola;
        }

        public PriceDataList getTfs() {
            return tfs;
        }

        public void setTfs(PriceDataList tfs) {
            this.tfs = tfs;
        }

        public PriceDataList getUber() {
            return uber;
        }

        public void setUber(PriceDataList uber) {
            this.uber = uber;
        }

        public Boolean getStatus() {
            return status;
        }

        public void setStatus(Boolean status) {
            this.status = status;
        }
    }
    class PriceDataList{
        private List<PriceData> dataList;
        Boolean status;

        @JsonProperty("data")
        public List<PriceData> getDataList() {
            return dataList;
        }

        @JsonProperty("data")
        public void setDataList(List<PriceData> dataList) {
            this.dataList = dataList;
        }

        public Boolean getStatus() {
            return status;
        }

        public void setStatus(Boolean status) {
            this.status = status;
        }
    }
    class PriceData {
        private String carCategory;
        private Integer estimate;
        private Integer highEstimate;
        private  Integer lowEstimate;

        @JsonProperty("high_estimate")
        public Integer getHighEstimate() {
            return highEstimate;
        }

        @JsonProperty("high_estimate")
        public void setHighEstimate(Integer highEstimate) {
            this.highEstimate = highEstimate;
        }

        @JsonProperty("low_estimate")
        public Integer getLowEstimate() {
            return lowEstimate;
        }

        @JsonProperty("low_estimate")
        public void setLowEstimate(Integer lowEstimate) {
            this.lowEstimate = lowEstimate;
        }

        @JsonProperty("car_category")
        public String getCarCategory() {
            return carCategory;
        }

        @JsonProperty("car_category")
        public void setCarCategory(String carCategory) {
            this.carCategory = carCategory;
        }

        public Integer getEstimate() {
            return estimate;
        }

        public void setEstimate(Integer estimate) {
            this.estimate = estimate;
        }
    }

