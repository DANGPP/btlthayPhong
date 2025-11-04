package com.example.noteapp.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.noteapp.fragment.CalendarFragment
import com.example.noteapp.fragment.StatisticsFragment
import com.example.noteapp.fragment.PomodoroFragment
import com.example.noteapp.fragment.SmartScheduleFragment
import com.example.noteapp.fragment.ProfileFragment


class PagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager,lifecycle) {

    override fun getItemCount(): Int {
        return 6
    }

    override fun createFragment(position: Int): Fragment {
       return when(position) {
           0 -> StatisticsFragment()
           1 -> ProfileFragment()
           2 -> CalendarFragment()
           3 -> PomodoroFragment()
           4 -> SmartScheduleFragment()
           5 -> StatisticsFragment()
           else -> CalendarFragment()
       }
    }



}