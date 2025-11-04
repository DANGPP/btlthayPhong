package com.example.noteapp.utils

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class HorizontalSpaceItemDecoration(private val horizontalSpaceWidth: Int) : RecyclerView.ItemDecoration() {
    
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        // Add spacing to the right of each item except the last one
        if (parent.getChildAdapterPosition(view) != (parent.adapter?.itemCount ?: 0) - 1) {
            outRect.right = horizontalSpaceWidth
        }
        
        // Add spacing to the left of the first item
        if (parent.getChildAdapterPosition(view) == 0) {
            outRect.left = horizontalSpaceWidth / 2
        }
    }
}