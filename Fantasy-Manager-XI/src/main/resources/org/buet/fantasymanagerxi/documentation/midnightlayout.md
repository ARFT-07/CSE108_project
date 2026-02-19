Excellent 👍 — using **Scene Builder** is the professional way to design JavaFX UIs visually.

I’ll now guide you step-by-step exactly how to build the **FantasyManagerXI homepage** in Scene Builder, make it responsive, connect it to your controller, and apply CSS styling properly.

No theory — just practical steps.

---

# ✅ PHASE 1 — Prepare Project Correctly

Before opening Scene Builder, confirm:

### 1️⃣ Folder Structure

```
src/main/java/com/fantasymanagerxi/controller/HomeController.java
src/main/resources/com/fantasymanagerxi/view/homepage.fxml
src/main/resources/style.css
```

⚠ Important: FXML and CSS must be in `resources`.

---

# ✅ PHASE 2 — Open Scene Builder & Create Layout

Open `homepage.fxml` in Scene Builder.

---

# 🧱 STEP-BY-STEP UI BUILD (Inside Scene Builder)

---

## 🟢 STEP 1 — Set Root Layout

1. Drag **BorderPane** into root (if not already).
2. Click BorderPane.
3. In the right panel → **Code Section**:

    * Set Controller Class:

```
com.fantasymanagerxi.controller.HomeController
```

4. In **Properties → Stylesheets**

    * Add: `@style.css`

---

## 🟢 STEP 2 — Build Left Panel (Teams Section)

1. Drag **VBox** into LEFT area of BorderPane.

2. Select VBox:

    * Spacing: `10`
    * Padding: `15`
    * Add Style Class: `side-panel`

3. Inside VBox add:

    * Label → Text: `Top 6 Teams`

        * Add Style Class: `section-title`
    * ListView

        * fx:id: `teamListView`

4. Select ListView:

    * In Layout → VBox Vgrow → `ALWAYS`
    * Pref Width: leave blank
    * Max Width: `Infinity`

✔ This makes ListView responsive vertically.

---

## 🟢 STEP 3 — Build Center Panel (League Table)

1. Drag **VBox** into CENTER of BorderPane.

2. VBox:

    * Spacing: 10
    * Padding: 20
    * Style Class: `center-panel`

3. Add Label:

    * Text: `League Table`
    * Style Class: `section-title`

4. Add **TableView**

    * fx:id: `leagueTableView`
    * VBox Vgrow: `ALWAYS`

---

### Add Columns (Inside Scene Builder)

Inside TableView → Add 4 TableColumns:

| Text   | fx:id       |
| ------ | ----------- |
| Pos    | positionCol |
| Team   | teamCol     |
| Points | pointsCol   |
| GD     | gdCol       |

Then:

* Select TableView
* In Layout → Column Resize Policy:

    * Choose `CONSTRAINED_RESIZE_POLICY`

This makes columns auto-resize when window changes size.

---

## 🟢 STEP 4 — Bottom Panel (Highlights Section)

1. Drag **StackPane** into BOTTOM.

2. Set:

    * Padding: 15
    * Style Class: `bottom-panel`

3. Add Label inside:

    * fx:id: `highlightLabel`
    * Text: `Matchday Highlights`
    * Style Class: `highlight-text`

---

# ✅ PHASE 3 — Make It Responsive (Important)

These are the 5 settings that make your layout responsive:

### ✔ 1. VBox Vgrow = ALWAYS

For:

* ListView
* TableView

### ✔ 2. Remove Fixed Heights

Delete Pref Height values unless required.

### ✔ 3. Use CONSTRAINED_RESIZE_POLICY

For TableView.

### ✔ 4. Set Max Width = Infinity

For ListView & TableView.

### ✔ 5. Set Minimum Window Size (in MainApp)

[//]: # (```java)

[//]: # (stage.setMinWidth&#40;900&#41;;)

[//]: # (stage.setMinHeight&#40;600&#41;;)

[//]: # (```)

---

# ✅ PHASE 4 — Create style.css

Create file:

```
resources/style.css
```

---

## style.css

```css
.root {
    -fx-background-color: linear-gradient(to bottom, #1c1c1c, #2a2a2a);
    -fx-font-family: "Segoe UI";
}

/* Left Panel */
.side-panel {
    -fx-background-color: #111111;
}

/* Section Titles */
.section-title {
    -fx-text-fill: gold;
    -fx-font-size: 18px;
    -fx-font-weight: bold;
}

/* Table Styling */
.table-view {
    -fx-background-color: #1e1e1e;
    -fx-border-color: gold;
}

.table-view .column-header-background {
    -fx-background-color: #333333;
}

.table-row-cell {
    -fx-text-fill: white;
}

/* Bottom Section */
.bottom-panel {
    -fx-background-color: #0f0f0f;
}

.highlight-text {
    -fx-text-fill: white;
    -fx-font-size: 16px;
}
```

---

# ✅ PHASE 5 — Controller Code (HomeController.java)

Now connect UI to backend.

---

## Full Basic Controller Setup

```java
package com.fantasymanagerxi.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.*;
import com.fantasymanagerxi.model.*;
import com.fantasymanagerxi.service.DataService;

public class HomeController {

    @FXML private ListView<Team> teamListView;
    @FXML private TableView<LeagueTableEntry> leagueTableView;

    @FXML private TableColumn<LeagueTableEntry, Integer> positionCol;
    @FXML private TableColumn<LeagueTableEntry, String> teamCol;
    @FXML private TableColumn<LeagueTableEntry, Integer> pointsCol;
    @FXML private TableColumn<LeagueTableEntry, Integer> gdCol;

    @FXML private Label highlightLabel;

    private DataService dataService = new DataService();

    @FXML
    public void initialize() {

        setupTeamList();
        setupLeagueTable();
        setupHighlights();
    }

    private void setupTeamList() {

        teamListView.setItems(dataService.getTeams());

        teamListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Team item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });
    }

    private void setupLeagueTable() {

        positionCol.setCellValueFactory(data ->
            new SimpleIntegerProperty(data.getValue().getPosition()).asObject());

        teamCol.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getTeamName()));

        pointsCol.setCellValueFactory(data ->
            new SimpleIntegerProperty(data.getValue().getPoints()).asObject());

        gdCol.setCellValueFactory(data ->
            new SimpleIntegerProperty(data.getValue().getGoalDifference()).asObject());

        leagueTableView.setItems(dataService.getLeagueTable());
    }

    private void setupHighlights() {

        highlightLabel.setText(
            "Matchday 1: Arsenal 3 - 1 Chelsea | City 2 - 0 Liverpool"
        );
    }
}
```

---

# 🧠 PROFESSIONAL TIP

If you want ultra-responsive behavior:

Add this inside initialize():

```java

[//]: # (leagueTableView.setColumnResizePolicy&#40;)

[//]: # (        TableView.CONSTRAINED_RESIZE_POLICY&#41;;)

[//]: # (```)

Even if you already set it in Scene Builder.

---

# 🧪 TEST RESPONSIVENESS

Now resize the window:

* ✅ Table grows horizontally
* ✅ Columns resize proportionally
* ✅ ListView stretches vertically
* ✅ Bottom panel stays anchored
* ✅ No overlapping

If all true → Layout is correctly responsive.

---

# 🎯 What You Have Achieved

You now have:

✔ SceneBuilder-based layout
✔ Proper MVC structure
✔ Responsive UI
✔ CSS styling
✔ Clean controller logic

---

# 🚀 Next Logical Step

After homepage layout is complete, you should:

1. Add clickable team navigation
2. Add animated hover effects
3. Add media player section
4. Add toolbar navigation

---

If you'd like, next I can show you:

* How to design a **modern football dashboard look**
* How to add **hover animations in CSS**
* Or how to make your UI look like a professional sports analytics system** 🔥
