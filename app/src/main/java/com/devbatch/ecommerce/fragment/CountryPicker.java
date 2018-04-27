package com.devbatch.ecommerce.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


import com.devbatch.ecommerce.R;
import com.devbatch.ecommerce.communication.network.Country;
import com.devbatch.ecommerce.utils.CommonKeys;
import com.devbatch.ecommerce.utils.CommonUtil;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class CountryPicker extends DialogFragment implements
        Comparator<Country> {
    /**
     * View components
     */
    private EditText searchEditText;
    private ListView countryListView;

    /**
     * Adapter for the listview
     */
    private CountryListAdapter adapter;

    /**
     * Hold all countries, sorted by country name
     */
    private List<Country> allCountriesList;

    /**
     * Hold countries that matched user query
     */
    private List<Country> selectedCountriesList;

    /**
     * Listener to which country user selected
     */
    private CountryPickerListener listener;

    /**
     * Set listener
     *
     * @param listener
     */
    public void setListener(CountryPickerListener listener) {
        this.listener = listener;
    }

    public EditText getSearchEditText() {
        return searchEditText;
    }

    public ListView getCountryListView() {
        return countryListView;
    }

    /**
     * Convenient function to get currency code from country code currency code
     * is in English locale
     *
     * @param countryCode
     * @return
     */
    public static Currency getCurrencyCode(String countryCode) {
        try {
            return Currency.getInstance(new Locale("en", countryCode));
        } catch (Exception e) {

        }
        return null;
    }

    /**
     * Get all countries with code and name from res/raw/countries.json
     *
     * @return
     */
    private List<Country> getAllCountries() {
        if (allCountriesList == null) {
            try {
                allCountriesList = new ArrayList<Country>();

                // Read from local file
                String allCountriesString = readFileAsString(getActivity());

                JSONObject jsonObject = new JSONObject(allCountriesString);
                Iterator<?> keys = jsonObject.keys();

                // Add the data to all countries list
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    Country country = new Country();
                    country.setCode(key);
                    country.setName(jsonObject.getString(key));
                    allCountriesList.add(country);
                }

                // Sort the all countries list based on country name
                Collections.sort(allCountriesList, this);

                // Initialize selected countries with all countries
                selectedCountriesList = new ArrayList<Country>();
                selectedCountriesList.addAll(allCountriesList);

                // Return
                return allCountriesList;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * R.string.countries is a json string which is Base64 encoded to avoid
     * special characters in XML. It's Base64 decoded here to get original json.
     *
     * @param context
     * @return
     * @throws java.io.IOException
     */
    private static String readFileAsString(Context context)
            throws java.io.IOException {
        String base64 = context.getResources().getString(R.string.countries);
        byte[] data = Base64.decode(base64, Base64.DEFAULT);
        return new String(data, "UTF-8");
    }


    public static CountryPicker showCountryPickerDialog(FragmentActivity context, String title, CountryPickerListener dialogCallback) {
        CountryPicker frag = new CountryPicker();
        Bundle bundle = new Bundle();
        bundle.putString(CommonKeys.TITLE, title);
        frag.setArguments(bundle);
        frag.setListener(dialogCallback);
        frag.show(context.getSupportFragmentManager(), "CountryPicker");
        return frag;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View rootView = LayoutInflater.from(getActivity()).inflate(R.layout.country_picker, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        alertDialog.setView(rootView, 10, 0, 10, 0);
        alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        initUiComponent(rootView);
        return alertDialog;
    }

    private void initUiComponent(View view) {
        // Get countries from the json
        getAllCountries();

        // Set dialog title if show as dialog
        Bundle args = getArguments();
        if (args != null) {
            String dialogTitle = args.getString(CommonKeys.TITLE);
            //getDialog().setTitle(dialogTitle);


        }

        // Get view components
        searchEditText = (EditText) view
                .findViewById(R.id.country_picker_search);
        countryListView = (ListView) view
                .findViewById(R.id.country_picker_listview);

        // Set adapter
        adapter = new CountryListAdapter(getActivity(), selectedCountriesList);
        countryListView.setAdapter(adapter);

        // Inform listener
        countryListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (listener != null) {
                    Country country = selectedCountriesList.get(position);
                    listener.onSelectCountry(country.getName(),
                            country.getCode());
                    dismiss();
                }
            }
        });

        // Search for which countries matched user query
        searchEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                search(s.toString());
            }
        });
    }

    /**
     * Search allCountriesList contains text and put result into
     * selectedCountriesList
     *
     * @param text
     */
    @SuppressLint("DefaultLocale")
    private void search(String text) {
        selectedCountriesList.clear();

        for (Country country : allCountriesList) {
            if (country.getName().toLowerCase(Locale.ENGLISH)
                    .contains(text.toLowerCase())) {
                selectedCountriesList.add(country);
            }
        }

        adapter.notifyDataSetChanged();
    }

    /**
     * Support sorting the countries list
     */
    @Override
    public int compare(Country lhs, Country rhs) {
        return lhs.getName().compareTo(rhs.getName());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        countryListView = null;
        allCountriesList = null;
    }

    public static class CountryListAdapter extends BaseAdapter {

        private Context context;
        List<Country> countries;
        LayoutInflater inflater;


        /**
         * Constructor
         *
         * @param context
         * @param countries
         */
        public CountryListAdapter(Context context, List<Country> countries) {
            super();
            this.context = context;
            this.countries = countries;
            inflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return countries.size();
        }

        @Override
        public Object getItem(int arg0) {
            return null;
        }

        @Override
        public long getItemId(int arg0) {
            return 0;
        }

        /**
         * Return row for each country
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View cellView = convertView;
            Cell cell;
            Country country = countries.get(position);

            if (convertView == null) {
                cell = new Cell();
                cellView = inflater.inflate(R.layout.row_country, null);
                cell.textView = (TextView) cellView.findViewById(R.id.row_title);
                cell.imageView = (ImageView) cellView.findViewById(R.id.row_icon);
                cellView.setTag(cell);
            } else {
                cell = (Cell) cellView.getTag();
            }

            cell.textView.setText(country.getName());

            // Load drawable dynamically from country code
            String drawableName = "flag_"
                    + country.getCode().toLowerCase(Locale.ENGLISH);
            cell.imageView.setImageResource(CommonUtil.getResId(drawableName));
            return cellView;
        }

        /**
         * Holder for the cell
         */
        static class Cell {
            public TextView textView;
            public ImageView imageView;
        }

    }

    public interface CountryPickerListener {
        void onSelectCountry(String name, String code);
    }

}
