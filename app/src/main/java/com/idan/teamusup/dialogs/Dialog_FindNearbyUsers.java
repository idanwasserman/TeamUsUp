//package com.idan.teamusup.dialogs;
//
//import android.app.AlertDialog;
//import android.app.Dialog;
//import android.content.Context;
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.widget.LinearLayout;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AppCompatDialogFragment;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.airbnb.lottie.LottieAnimationView;
//import com.google.android.material.textview.MaterialTextView;
//import com.idan.teamusup.adapters.PlayerAdapter_Big;
//import com.idan.teamusup.R;
//import com.idan.teamusup.data.Instance;
//
//import java.util.ArrayList;
//import java.util.List;
//
//
//public class Dialog_FindNearbyUsers extends AppCompatDialogFragment {
//
//    private FindNearbyUsersDialogListener findNearbyUsersDialogListener;
//
//    private LottieAnimationView lottie_SPC_searching;
//    private LinearLayout dialog_linear_layout;
//
//    private ArrayList<Instance> usersInstances;
//    private RecyclerView dialog_LIST_users;
//    private PlayerAdapter_Big adapterPlayer;
//    private MaterialTextView dialog_TXT_chosenUsers;
//    private boolean[] chosenPositions;
//    private String[] chosenUsersNames;
//    private List<Instance> chosenUsers;
//
//    public Dialog_FindNearbyUsers(
//            FindNearbyUsersDialogListener findNearbyUsersDialogListener,
//            ArrayList<Instance> playersInstances) {
//        this.findNearbyUsersDialogListener = findNearbyUsersDialogListener;
//        this.usersInstances = playersInstances;
//        this.chosenPositions = new boolean[this.usersInstances.size()];
//        this.chosenUsersNames = new String[this.usersInstances.size()];
//        this.chosenUsers = new ArrayList<>();
//    }
//
//    @NonNull
//    @Override
//    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        LayoutInflater inflater = requireActivity().getLayoutInflater();
//        View view = inflater.inflate(R.layout.dialog_find_nearby_users, null);
//        findViews(view);
//
//
//
//        builder
//                .setView(view)
//                .setTitle("Find nearby users")
//                .setNegativeButton(R.string.cancel, (dialog, which) -> {
//                    // Do nothing
//                })
//                .setPositiveButton(R.string.submit, (dialog, which) ->
//                        findNearbyUsersDialogListener.submit(null));
//
//        return builder.create();
//    }
//
//    @Override
//    public void onAttach(@NonNull Context context) {
//        super.onAttach(context);
//    }
//
//    public interface FindNearbyUsersDialogListener {
//        void submit(List<Instance> users);
//    }
//
//    private void findViews(View view) {
//        this.lottie_SPC_searching = view.findViewById(R.id.lottie_SPC_searching);
//        this.dialog_linear_layout = view.findViewById(R.id.dialog_linear_layout);
//
//        this.dialog_LIST_users = view.findViewById(R.id.dialog_LIST_users);
//        this.dialog_TXT_chosenUsers = view.findViewById(R.id.dialog_TXT_chosenUsers);
//    }
//
//}
