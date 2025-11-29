# ILP-Cw3 — How to run

This README explains how to run this project, running the Application.yml in ILP-Cw2 and starting the react project for ilp-visualiser.

Prerequisites
- npm installed
- npm packages installed
- PowerShell (commands below are written for PowerShell on Windows)

Notes
- The provided ILP-Cw2 project is built using Maven. All that needs to be done to start the application is to run `Application`.
- The provided react project is built using npm. All that needs to be done to start the application is to run `npm run dev` in the `ilp-visualiser` directory.

Checklist
- Ensure you have the prerequisites installed
- Start the application with `Application` in ILP-Cw2.
- Start the React project with `npm run dev` in the `ilp-visualiser` directory.

Step 1 — If npm is not installed or the required packages aren't installed, install it in the React project directory.
From the project root cd to ilp-visualiser.

```powershell
cd ilp-visualiser
npm install
```

Install the required packages.
```powershell
npm install axios react-router-dom
npm install leaflet react-leaflet
npm install --save-dev @types/leaflet
```

Step 2 — Start the React project.

```powershell
npm run dev
```
This will start the frontend React project on localhost:5173.

Vite will show something like:

```powershell
  VITE v4.3.9  ready in 300 ms

  ➜  Local:   http://localhost:5173/
  ➜  Network: use --host to expose
```

Step 3 — Run the `Application` in the ILP-Cw2 project directory.

In intellij, run the Application class.

This will start the backend application on localhost:8080.

Step 4 — Open the React project in your browser.

This should open the frontend React project on localhost:5173.

Once this has opened, it will show a MedDispatchRec planner. Where the user can create as many MedDispatchRecs as they want. The only required fields (as per the ILP spec) are ID (which is automatically filled), Capacity, and the delivery co-ordinates. The user can click anywhere on the map and the related lng/lat will be filled into the Dispatch form. 

Once a MedDispatchRec has been added it will show up in a table of all current MedDispatchRec's, there is a delete button if the user chooses to delete a MedDispatchRec. Once the user has created as many MedDispatchRecs as they want, they can click on the "Calculate Delivery Path" button to see the path that the Drones will take.

This will open a map showing the delivery path. This map is interactive, and has a playback button that allows the user to step through the delivery path. From the Service Points to the Delivery Point.