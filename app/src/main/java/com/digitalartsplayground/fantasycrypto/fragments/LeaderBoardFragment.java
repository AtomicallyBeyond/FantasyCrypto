package com.digitalartsplayground.fantasycrypto.fragments;

import android.app.Activity;
import android.content.Intent;
import android.opengl.Visibility;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.digitalartsplayground.fantasycrypto.MainActivity;
import com.digitalartsplayground.fantasycrypto.R;
import com.digitalartsplayground.fantasycrypto.models.CryptoAsset;
import com.digitalartsplayground.fantasycrypto.models.LimitOrder;
import com.digitalartsplayground.fantasycrypto.models.MarketUnit;
import com.digitalartsplayground.fantasycrypto.mvvm.viewmodels.LeaderBoardViewModel;
import com.digitalartsplayground.fantasycrypto.util.AppExecutors;
import com.digitalartsplayground.fantasycrypto.util.SharedPrefs;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.games.leaderboard.Leaderboard;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.games.leaderboard.ScoreSubmissionData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.ironsource.mediationsdk.O;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LeaderBoardFragment extends Fragment {

    private LeaderBoardViewModel leaderBoardViewModel;
    private GoogleSignInClient googleSignInClient;
    private LeaderboardsClient leaderboardsClient;
    private ProgressBar progressBar;
    private ImageView leaderBoardImage;
    private TextView redirectingMessage;
    private float balance;

    public LeaderBoardFragment() {
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        leaderBoardViewModel = new ViewModelProvider(requireActivity())
                .get(LeaderBoardViewModel.class);
        balance = SharedPrefs.getInstance(requireContext()).getBalance();
        initGoogleClientSignIn();
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.leader_board_fragment, container, false);
        progressBar = view.findViewById(R.id.leaderboard_progress_bar);
        leaderBoardImage = view.findViewById(R.id.leaderboard_loading_logo);
        redirectingMessage = view.findViewById(R.id.leaderboard_redirect_message);
        return view;
    }

    private void initGoogleClientSignIn() {

        GoogleSignInOptions googleSignInOptions = GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN;

        googleSignInClient = GoogleSignIn.getClient(requireContext(), googleSignInOptions);
        googleSignInClient.silentSignIn().addOnCompleteListener(requireActivity(), new OnCompleteListener<GoogleSignInAccount>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<GoogleSignInAccount> task) {
                if(task.isSuccessful()) {

                    leaderboardsClient = Games.getLeaderboardsClient(getActivity(), task.getResult());

                    if(leaderboardsClient != null)
                        uploadPortfolioValue();

                } else {

                    task.getException().fillInStackTrace();
                }
            }
        });
    }

    private void uploadPortfolioValue(){

        AppExecutors.getInstance().diskIO().execute(new Runnable() {

            private float value = 0;
            private MarketUnit marketUnit;

            @Override
            public void run() {

                List<CryptoAsset> assets = leaderBoardViewModel.getAllAssets();

                if(assets != null) {

                    for(CryptoAsset asset :assets) {

                        marketUnit = leaderBoardViewModel.fetchMarketUnit(asset.getId());
                        if(marketUnit != null) {
                            value += marketUnit.getCurrentPrice() * asset.getAmount();
                        }
                    }
                }

                List<LimitOrder> buyActiveOrders = leaderBoardViewModel.fetchBuyOrders();
                for(LimitOrder limitOrder : buyActiveOrders) {
                    value += limitOrder.getValue();
                }

                List<LimitOrder> sellActiveOrders = leaderBoardViewModel.fetchSellOrders();
                for(LimitOrder limitOrder : sellActiveOrders) {
                    value += (leaderBoardViewModel.fetchMarketUnit(limitOrder.getCoinID()).getCurrentPrice()
                            * limitOrder.getAmount());
                }

                value += balance;

                if(value > 10000f) {
                    long formattedValue = (long)(value * 1000000f);

                    leaderboardsClient
                            .submitScoreImmediate(getString(R.string.leaderboard_fantasy_crypto), formattedValue)
                            .addOnSuccessListener(new OnSuccessListener<ScoreSubmissionData>() {
                                @Override
                                public void onSuccess(ScoreSubmissionData scoreSubmissionData) {

                                    leaderboardsClient.getAllLeaderboardsIntent().addOnSuccessListener(new OnSuccessListener<Intent>() {
                                        @Override
                                        public void onSuccess(Intent intent) {
                                            startActivityForResults(intent);
                                        }
                                    });
                                }
                            });
                }
            }//end run
        });//end execute
    }

    private void startActivityForResults(Intent intent) {

        showLoadingScreen(View.GONE);
        someActivityResultLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    if(getActivity() != null)
                        ((MainActivity)getActivity()).setMarketTab();
                }
            });

    private void showLoadingScreen(int visibility) {
        progressBar.setVisibility(visibility);
        leaderBoardImage.setVisibility(visibility);
        redirectingMessage.setVisibility(visibility);
    }
}
