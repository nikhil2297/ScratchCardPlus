# ScratchCardPlus
Using single layout you can add multiple element behind the scratch view

## Getting Started

There are simple attr that you should know

app:erase_stroke_width = Erase brush width 

**Note : Assume brush as you finger :stuck_out_tongue_closed_eyes:**

app:reveal_percent= When give value is visible on your view is reveal the view

**The moste important one you have add these two attr in all your scratchcard layout**

app:scratch_on = Here you can add either a drawable or color

app:scratch_type = Here you have to add type for drawable or color


**Example**

```
<com.scratchcard.ScratchLayout
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        app:scratch_on="@drawable/ic_launcher_background"
        app:scratch_type="drawable"
        app:erase_stroke_width="50"
        app:reveal_percent="40"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent">
        
        //Any view you want to add

    </com.scratchcard.ScratchLayout>
    
