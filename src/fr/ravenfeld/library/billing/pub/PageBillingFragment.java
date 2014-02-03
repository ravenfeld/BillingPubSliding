package fr.ravenfeld.library.billing.pub;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;



import net.robotmedia.billing.BillingController;
import net.robotmedia.billing.BillingRequest;
import net.robotmedia.billing.helper.AbstractBillingFragment;
import net.robotmedia.billing.model.Transaction;

import java.util.List;

public class PageBillingFragment extends AbstractBillingFragment {

    public static final String ID_PRODUCT = "android.test.purchased";

    public static final String KEY_PREF_BILLING = "billing";

    public static final String KEY_PUBLIC_BILLING = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtdSiigao6RnaMQ4u952Xrrt4vlLCRuS+KubN/5o+bQfFPZqZbMWPlnY7ifpT/dlCo3+v+QZJzmcic3myCr2CygJNM9J8MuLVBx9mBkWDWZpBIRZBeuRwbnqN2vgmCoNjS3xv2/+hKMLkImnRp5kxUt6GJFYltIp1qh1ircekTfJoziH62HgL4qboNJGkWVyV+q96lcQxB6OY87vtUPkNmkGQoSWGWZkzvipXSZI5XuJZUppcxmGLedVFr0Vy0Ii2PVd7jjpkiMZCYW+E0hKmUJHtuSTKzjRyQXZhmvvk+aFSthlhtzio5eDN8oUFGKmf/IGv9y07Y5ALFjyW9da5vwIDAQAB";

    private SharedPreferences mSharedPreferences;

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
        mSharedPreferences = getActivity().getSharedPreferences(AppConstants.SHARED_PREFS_NAME, 0);

        View view  = inflater.inflate(R.layout.page_billing_layout, container, false);

        ImageButton button = (ImageButton) view.findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BillingController.requestPurchase(getActivity(), ID_PRODUCT);
            }
        });
        ImageButton quit = (ImageButton) view.findViewById(R.id.quit);
        quit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });
        BillingController.registerObserver(mBillingObserver);
        BillingController.checkBillingSupported(getActivity());
        return view ;
	}

    @Override
    public void onBillingChecked(boolean supported) {
        if (supported) {
            restoreTransactions();
        } else {
            getActivity().showDialog(R.string.billing_not_supported_message);
        }
    }

    @Override
    public void onSubscriptionChecked(boolean supported) {

    }

    @Override
    public void onPurchaseStateChanged(String id_product, Transaction.PurchaseState status) {
        if (id_product.equalsIgnoreCase(ID_PRODUCT) && status == Transaction.PurchaseState.PURCHASED) {
            purchasedApp();
               getActivity().finish();

        } else {
            canceledApp();
        }
    }

    @Override
    public void onRequestPurchaseResponse(String id_product, BillingRequest.ResponseCode code) {
        if (id_product.equalsIgnoreCase(ID_PRODUCT) && code == BillingRequest.ResponseCode.RESULT_OK) {
            purchasedApp();
            getActivity().finish();
        } else {
            canceledApp();
        }
    }

    @Override
    public byte[] getObfuscationSalt() {
        return new byte[]{41, -90, -116, -41, 66, -53, 122, -110, -127, -96, -88, 77, 127, 115, 1, 73, 57, 110, 48, -116};
    }

    @Override
    public String getPublicKey() {
        return KEY_PUBLIC_BILLING;
    }

    @Override
    public void restoreTransactions() {
        super.restoreTransactions();

        BillingController.restoreTransactions(getActivity());
        checkPurchase();
    }

    private void checkPurchase() {
        if (ID_PRODUCT != null) {
            List<Transaction> transactions = BillingController.getTransactions(this.getActivity(), ID_PRODUCT);
            if (transactions.isEmpty()) {
            }
            for (Transaction transaction : transactions) {
                if (transaction.purchaseState == Transaction.PurchaseState.PURCHASED) {
                    getActivity().finish();
                } else {
                    canceledApp();
                    //showPopup();
                }
            }
        }
    }

    private void purchasedApp() {
        SharedPreferences.Editor prefEditor = mSharedPreferences.edit();
        prefEditor.putBoolean(KEY_PREF_BILLING, true);
        prefEditor.commit();
    }

    private void canceledApp() {
        SharedPreferences.Editor prefEditor = mSharedPreferences.edit();
        prefEditor.putBoolean(KEY_PREF_BILLING, false);
        prefEditor.commit();
    }
}
