package com.point2points.kdusurveysystem.adapter.student;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.daimajia.swipe.SimpleSwipeListener;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.point2points.kdusurveysystem.Fragment.SurveyQuestionPagerActivity;
import com.point2points.kdusurveysystem.Login;
import com.point2points.kdusurveysystem.R;
import com.point2points.kdusurveysystem.RecyclerView.RecyclerViewSurvey;
import com.point2points.kdusurveysystem.adapter.util.RecyclerLetterIcon;
import com.point2points.kdusurveysystem.datamodel.Survey;
import com.point2points.kdusurveysystem.datamodel.SurveyStudent;
import com.point2points.kdusurveysystem.student.StudentHome;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class StudentHomeRecyclerViewAdapter extends RecyclerSwipeAdapter<StudentHomeRecyclerViewAdapter.SimpleViewHolder> {

    static DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
    static Query query;

    private Context mContext;
    private static ArrayList<Survey> surveys = new ArrayList<>();
    public static ArrayList<Survey> SurveyDataSet = new ArrayList<>();
    private SurveyStudent mSurveyStudent = new SurveyStudent();

    static int retrieveCounter;

    private static FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private static String UID = user.getUid();

    public class SimpleViewHolder extends android.support.v7.widget.RecyclerView.ViewHolder {
        SwipeLayout swipeLayout;
        TextView textViewSurveySubjectName;
        TextView textViewSurveyStatus;
        TextView textViewSurveySubjectCode;
        TextView textViewSurveySubjectSchool;
        TextView textViewSurveyLecturer;
        TextView textViewSurveyUid;
        ImageButton buttonStartSurvey;
        ImageView letterimage;

        public SimpleViewHolder(final View itemView) {
            super(itemView);
            swipeLayout = (SwipeLayout) itemView.findViewById(R.id.swipe);
            textViewSurveySubjectName = (TextView) itemView.findViewById(R.id.student_survey_name_text_view);
            textViewSurveyStatus = (TextView) itemView.findViewById(R.id.student_survey_status_text_view);
            textViewSurveySubjectSchool = (TextView) itemView.findViewById(R.id.student_survey_subject_school_text_view);
            textViewSurveyLecturer = (TextView) itemView.findViewById(R.id.student_survey_lecturer_text_view);
            textViewSurveySubjectCode = (TextView) itemView.findViewById(R.id.student_survey_subject_code_text_view);
            textViewSurveyUid = (TextView) itemView.findViewById(R.id.student_survey_uid_text_view);
            buttonStartSurvey = (ImageButton) itemView.findViewById(R.id.start_survey);
            letterimage = (ImageView) itemView.findViewById(R.id.letter_icon);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Toast toastItemOnClick;
                    toastItemOnClick = Toast.makeText(mContext, "Double Tap to Start this Survey", Toast.LENGTH_SHORT);
                    toastItemOnClick.show();

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            toastItemOnClick.cancel();
                        }
                    }, 500);
                }
            });
        }
    }

    public StudentHomeRecyclerViewAdapter(Context context) {
        this.mContext = context;
        FirebaseSurveyDataRetrieval();
    }

    @Override
    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.student_home_recycler_view_items, parent, false);
        return new SimpleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final SimpleViewHolder viewHolder, final int position) {

        final Survey item = SurveyDataSet.get(position);
        String surveySubjectName = item.getSurveySubject();
        String surveySubjectCode = item.getSurveySubjectCode();
        String surveySubjectCategory = item.getSurveySubjectCategory();
        String surveyLecturer = item.getSurveyLecturer();
        String surveySchoolShort = item.getSurveySchoolShort();

        viewHolder.swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);
        viewHolder.swipeLayout.addSwipeListener(new SimpleSwipeListener() {
            @Override
            public void onOpen(SwipeLayout layout) {
                YoYo.with(Techniques.Swing).duration(1000).delay(100).playOn(layout.findViewById(R.id.start_survey));
            }
        });
        viewHolder.swipeLayout.setOnDoubleClickListener(new SwipeLayout.DoubleClickListener() {
            @Override
            public void onDoubleClick(SwipeLayout layout, boolean surface) {
                checkSurveyStatus(viewHolder.swipeLayout.getContext(), item.getSurveyUID());
            }
        });
        viewHolder.buttonStartSurvey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                checkSurveyStatus(view.getContext(), item.getSurveyUID());
            }
        });

        TextDrawable drawable = RecyclerLetterIcon.GenerateRecyclerLetterIcon(surveySubjectName, 64);

        viewHolder.letterimage.setImageDrawable(drawable);
        viewHolder.textViewSurveySubjectName.setText(surveySubjectName);

        Log.e(Long.toString(item.getSurveyAttendance()), Long.toString(item.getSurveyTotalAttendance()));
        //Log.e("ATTENDANCE", Long.toString(item.getSurveyAttendance()));
        //Log.e("TOTAL", Long.toString(item.getSurveyTotalAttendance()));

        viewHolder.textViewSurveyStatus.setText("Attendance: " + item.getSurveyAttendance() + "/" + item.getSurveyTotalAttendance());
        //viewHolder.textViewSurveyStatus.setText("Attendance: " + getAttendance(studentsAttendance, position) + "/" + getTotalStudents(totalStudents, position));
        viewHolder.textViewSurveySubjectSchool.setText(surveySchoolShort);
        viewHolder.textViewSurveyLecturer.setText(surveyLecturer);
        viewHolder.textViewSurveySubjectCode.setText(surveySubjectCode + "\t(" + surveySubjectCategory + ")");
        viewHolder.textViewSurveyUid.setText(item.getSurveyUID());
        mItemManger.bindView(viewHolder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return SurveyDataSet.size();
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe;
    }

    public static void filter(String charText) {

        charText = charText.toLowerCase(Locale.getDefault());
        SurveyDataSet = new ArrayList<Survey>();

        if (charText.length() == 0) {
            SurveyDataSet = surveys;
        } else {
            for (Survey survey : surveys) {
                if (survey.getSurveySubject().toLowerCase(Locale.getDefault()).contains(charText)
                        || survey.getSurveySubjectCode().toLowerCase(Locale.getDefault()).contains(charText)
                        || survey.getSurveySubjectCategory().toLowerCase(Locale.getDefault()).contains(charText)
                        || survey.getSurveySchool().toLowerCase(Locale.getDefault()).contains(charText)
                        || survey.getSurveySchoolShort().toLowerCase(Locale.getDefault()).contains(charText)
                        || survey.getSurveySubject().toLowerCase(Locale.getDefault()).contains(charText)
                        || survey.getSurveyLecturer().toLowerCase(Locale.getDefault()).contains(charText)
                        || survey.getSurveyLecturerId().toLowerCase(Locale.getDefault()).contains(charText)) {
                    SurveyDataSet.add(survey);
                }
            }
            StudentHome.notifyDataChanges();
        }
    }

    public static void FirebaseSurveyDataRetrieval() {

        StudentHome.onProgressBar();
        retrieveCounter = 0;

        surveys = new ArrayList<>();
        SurveyDataSet = new ArrayList<>();

        user = FirebaseAuth.getInstance().getCurrentUser();
        UID = user.getUid();
        //final String UID = Login.loginUID;

        ref = FirebaseDatabase.getInstance().getReference().child("survey");
        query = ref;

        // ListneverForSignleValueEvent to avoid repeated refreshes on multiple simultaneous data updates (from SurveyQuestionFragment).
        //query.addValueEventListener(new ValueEventListener() {
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Log.e("Count ", "" + snapshot.getChildrenCount());
                for (final DataSnapshot postSnapshot : snapshot.getChildren()) {
                    retrieveCounter++;
                    boolean found = false;
                    for (Survey survey : surveys) {
                        if (survey.getSurveyUID() == postSnapshot.getValue(Survey.class).getSurveyUID()) {
                            found = true;
                        }
                    }
                    if (!found) {
                        query = ref.child(postSnapshot.getValue(Survey.class).getSurveyUID()).child("student");
                        //query.addValueEventListener(new ValueEventListener() {
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                //Log.e("DO I RUN 2", "YES 2");

                                //get the involved survey for the following student
                                for (final DataSnapshot postSnapshot2 : dataSnapshot.getChildren()) {
                                    if (UID.equals(postSnapshot2.getValue(SurveyStudent.class).getSurveyStudentUID())) {

                                        surveys.add(postSnapshot.getValue(Survey.class));

                                        if (postSnapshot.getChildrenCount() >= retrieveCounter || postSnapshot.getChildrenCount() == 0) {

                                            //copy data set and sort
                                            SurveyDataSet = surveys;
                                            Collections.sort(SurveyDataSet, new Comparator<Survey>()

                                                    {
                                                        @Override
                                                        public int compare(Survey survey1, Survey survey2) {
                                                            return survey1.getSurveyDate().compareTo(survey2.getSurveyDate());
                                                        }
                                                    }
                                            );
                                            Collections.reverse(SurveyDataSet);

                                            StudentHome.offProgressBar();
                                            StudentHome.notifyDataChanges();
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.e("The read failed: ", databaseError.getMessage());
                            }
                        });
                    }
                }
                //Log.e("Size 1: ", String.valueOf(surveys.size()));
                //Log.e("Size 2: ", String.valueOf(snapshot.getChildrenCount()));
                // Execute block if no data (no surveys). Turn off progress bar.
                if (surveys.size() == snapshot.getChildrenCount())
                    StudentHome.offProgressBar();
            }

            /*
            @Override
            public void onDataChange(final DataSnapshot snapshot) {

                final long totalCount = snapshot.getChildrenCount();

                for (final DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Log.e("DO I RUN", "YES");
                    retrieveCounter++;

                    query = ref.child(postSnapshot.getValue(Survey.class).getSurveyUID()).child("student");
                    query.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Log.e("DO I RUN 2", "YES 2");
                            int completion = 0;
                            total = dataSnapshot.getChildrenCount();

                            //get attendance
                            for (final DataSnapshot postSnapshot2 : dataSnapshot.getChildren()) {
                                if (postSnapshot2.getValue(SurveyStudent.class).isSurveyStudentStatus()) {
                                    completion++;
                                }
                            }

                            DatabaseReference mDatabase;
                            mDatabase = FirebaseDatabase.getInstance().getReference().child("survey").child(postSnapshot.getValue(Survey.class).getSurveyUID());
                            Map<String, Object> updateSurvey = new HashMap<String, Object>();

                            updateSurvey.put("surveyTotalAttendance", total);
                            updateSurvey.put("surveyAttendance", completion);

                            mDatabase.updateChildren(updateSurvey);
                            Log.e("Get Data", "Completion: " + completion + " | Total: " + total);

                            //get the involved survey for the following student
                            for (final DataSnapshot postSnapshot2 : dataSnapshot.getChildren()) {
                                if (UID.equals(postSnapshot2.getValue(SurveyStudent.class).getSurveyStudentUID())) {

                                    Survey surveyAdd;
                                    surveyAdd = postSnapshot.getValue(Survey.class);
                                    surveyAdd.setSurveyAttendance(completion);
                                    surveyAdd.setSurveyTotalAttendance(total);
                                    surveys.add(surveyAdd);

                                    Log.e(Integer.toString(retrieveCounter) + " ", Long.toString(total));
                                    if (snapshot.getChildrenCount() >= retrieveCounter || snapshot.getChildrenCount() == 0) {
                                        StudentHome.offProgressBar();
                                        StudentHome.notifyDataChanges();
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e("The read failed: ", databaseError.getMessage());
                        }
                    });
                    Log.e("Get Data", (postSnapshot.getValue(Survey.class).getSurveySubject()));
                }
            }
 */
            @Override
            public void onCancelled(DatabaseError firebaseError) {
                Log.e("The read failed: ", firebaseError.getMessage());
            }
        });
    }

    public static void sortingData(int sortoption) {

        //FirebaseSurveyDataRetrieval();    // Was causing issues with sorting and duplicated data due to execution on view load.

        switch (sortoption) {
            case 1:
                Collections.sort(SurveyDataSet, new Comparator<Survey>() {
                    @Override
                    public int compare(Survey survey1, Survey survey2) {
                        return (survey1.getSurveySubject().substring(0, 1).toUpperCase() + survey1.getSurveySubject().substring(1)).compareTo(survey2.getSurveySubject().substring(0, 1).toUpperCase() + survey2.getSurveySubject().substring(1));
                    }
                });

                break;
            case 2:
                Collections.sort(SurveyDataSet, new Comparator<Survey>() {
                    @Override
                    public int compare(Survey survey1, Survey survey2) {
                        return (survey1.getSurveySubject().substring(0, 1).toUpperCase() + survey1.getSurveySubject().substring(1)).compareTo(survey2.getSurveySubject().substring(0, 1).toUpperCase() + survey2.getSurveySubject().substring(1));
                    }
                });
                Collections.reverse(SurveyDataSet);
                break;
            case 3:
                Collections.sort(SurveyDataSet, new Comparator<Survey>() {
                    @Override
                    public int compare(Survey survey1, Survey survey2) {
                        return survey1.getSurveyDate().compareTo(survey2.getSurveyDate());
                    }
                });
                Collections.reverse(SurveyDataSet);
                break;
            case 4:
                Collections.sort(SurveyDataSet, new Comparator<Survey>() {
                    @Override
                    public int compare(Survey survey1, Survey survey2) {
                        return survey1.getSurveyDate().compareTo(survey2.getSurveyDate());
                    }
                });
                break;
            default:
                break;
        }
        StudentHome.notifyDataChanges();
    }

    public static void SurveyArrayListUpdate(ArrayList updatedSurveys) {
        surveys = updatedSurveys;
        StudentHome.notifyDataChanges();
    }

    /*public static void SurveyArrayListUpdate() {
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Log.e("Count ", "" + snapshot.getChildrenCount());
                for (final DataSnapshot postSnapshot : snapshot.getChildren()) {
                    retrieveCounter++;
                    boolean found = false;
                    for (Survey survey : surveys) {
                        if (survey.getSurveyUID() == postSnapshot.getValue(Survey.class).getSurveyUID()) {
                            found = true;
                        }
                    }
                    if (!found) { query = ref.child(postSnapshot.getValue(Survey.class).getSurveyUID()).child("student");
                        //query.addValueEventListener(new ValueEventListener() {
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Log.e("DO I RUN 2", "YES 2");

                                //get the involved survey for the following student
                                for (final DataSnapshot postSnapshot2 : dataSnapshot.getChildren()) {
                                    if (UID.equals(postSnapshot2.getValue(SurveyStudent.class).getSurveyStudentUID())) {

                                        surveys.add(postSnapshot.getValue(Survey.class));

                                        if (postSnapshot.getChildrenCount() >= retrieveCounter || postSnapshot.getChildrenCount() == 0) {
                                            StudentHome.offProgressBar();
                                            StudentHome.notifyDataChanges();
                                        }
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.e("The read failed: ", databaseError.getMessage());
                            }
                        });
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError firebaseError) {
                Log.e("The read failed: ", firebaseError.getMessage());
            }
        });
    }*/

    public void checkSurveyStatus(final Context context, final String surveyUID) {

        user = FirebaseAuth.getInstance().getCurrentUser();
        UID = user.getUid();
        //final String UID = Login.loginUID;

        ref = FirebaseDatabase.getInstance().getReference().child("survey").child(surveyUID).child("student");
        query = ref;
        //query.addValueEventListener(new ValueEventListener() {
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (final DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Log.e("test 1", postSnapshot.getValue(SurveyStudent.class).getSurveyStudentName());
                    if (UID.equals(postSnapshot.getValue(SurveyStudent.class).getSurveyStudentUID())) {
                        Log.e("test 2", postSnapshot.getValue(SurveyStudent.class).getSurveyStudentUID());
                        mSurveyStudent = postSnapshot.getValue(SurveyStudent.class);
                        if (!mSurveyStudent.isSurveyStudentStatus()) {
                            Intent intent = SurveyQuestionPagerActivity.newIntent(context, surveyUID);
                            context.startActivity(intent);
                        } else {
                            Toast.makeText(context, "You have already done this survey", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                Log.e("The read failed: ", firebaseError.getMessage());
            }
        });
    }
}
