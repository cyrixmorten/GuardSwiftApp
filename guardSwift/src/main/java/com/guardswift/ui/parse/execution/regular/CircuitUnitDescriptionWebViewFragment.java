//package com.guardswift.ui.parse.execution.circuit;
//
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.webkit.WebChromeClient;
//import android.webkit.WebSettings;
//import android.webkit.WebView;
//import android.webkit.WebViewClient;
//import android.widget.Toast;
//
//import com.guardswift.R;
//import com.guardswift.dagger.InjectingFragment;
//import com.guardswift.eventbus.events.UpdateUIEvent;
//import com.guardswift.persistence.cache.task.CircuitUnitCache;
//import com.guardswift.persistence.parse.execution.task.regular.CircuitUnit;
//import com.guardswift.ui.GuardSwiftApplication;
//
//import javax.inject.Inject;
//
//import butterknife.BindView;
//import butterknife.ButterKnife;
//import butterknife.Unbinder;
//
//public class CircuitUnitDescriptionWebViewFragment extends InjectingFragment {
//
//    protected static final String TAG = CircuitUnitDescriptionWebViewFragment.class
//            .getSimpleName();
//
//    public static CircuitUnitDescriptionWebViewFragment newInstance(CircuitUnit circuitUnit) {
//
//        GuardSwiftApplication.getInstance().getCacheFactory().getCircuitUnitCache().setSelected(circuitUnit);
//
//        CircuitUnitDescriptionWebViewFragment fragment = new CircuitUnitDescriptionWebViewFragment();
//        Bundle args = new Bundle();
//        fragment.setArguments(args);
//        return fragment;
//    }
//
//    public CircuitUnitDescriptionWebViewFragment() {
//    }
//
//    @Inject
//    CircuitUnitCache circuitUnitCache;
//
//    @BindView(R.id.webView) WebView webView;
//
//    private CircuitUnit mCircuitUnit;
//
//    private Unbinder unbinder;
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        mCircuitUnit = circuitUnitCache.getSelected();
//    }
//
//
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        View rootView = inflater.inflate(R.layout.webview,
//                container, false);
//
//        unbinder = ButterKnife.bind(this, rootView);
//
//        WebSettings settings = webView.getSettings();
//        settings.setDefaultTextEncodingName("utf-8");
////        settings.setJavaScriptEnabled(true);
////        settings.setLoadWithOverviewMode(true);
////        settings.setUseWideViewPort(true);
//        settings.setBuiltInZoomControls(true);
////        settings.setSupportZoom(true);
//
//        webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
//        webView.setWebChromeClient(new WebChromeClient() {
//
//            @Override
//            public void onProgressChanged(WebView view, int newProgress) {
//                super.onProgressChanged(view, newProgress);
//            }
//
//        });
//        webView.setWebViewClient(new WebViewClient() {
//            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
//                if (getActivity() != null)
//                    Toast.makeText(getActivity(), "Oh no! " + description, Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        webView.clearCache(true);
//
//        String description = mCircuitUnit.getDescription();
//        if (description == null) {
//            description = getString(R.string.no_desc_regular_html);
//        }
//        webView.loadDataWithBaseURL("", description, "text/html", "utf-8", null);
//
//        return rootView;
//    }
//
//
//    public void onEventMainThread(UpdateUIEvent ev) {
//        // ignore
//    }
//
//
//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        unbinder.unbind();
//    }
//
//
//}
