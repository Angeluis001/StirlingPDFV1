var toggleCount = 0
var lastToggleTime = Date.now()

var elements = {
  lightModeStyles: null,
  darkModeStyles: null,
  rainbowModeStyles: null,
  darkModeIcon: null,
}

function getElements() {
  elements.lightModeStyles = document.getElementById("light-mode-styles")
  elements.darkModeStyles = document.getElementById("dark-mode-styles")
  elements.rainbowModeStyles = document.getElementById("rainbow-mode-styles")
  elements.darkModeIcon = document.getElementById("dark-mode-icon")
  elements.searchBar = document.getElementById("searchBar")
  elements.formControls = document.querySelectorAll(".form-control")
}

function setMode(mode) {
  var event = new CustomEvent("modeChanged", { detail: mode })
  document.dispatchEvent(event)
  elements.lightModeStyles.disabled = mode !== "off"
  elements.darkModeStyles.disabled = mode !== "on"
  elements.rainbowModeStyles.disabled = mode !== "rainbow"
  var jumbotron = document.getElementById("jumbotron")
  if (mode === "on") {
    elements.darkModeIcon.src = "moon.svg"
    // Dark mode improvement
    elements.searchBar.classList.add("dark-mode-search")
    elements.formControls.forEach(input => input.classList.add("bg-dark", "text-white"))
    // Add the table-dark class to tables for dark mode
    var tables = document.querySelectorAll(".table")
    tables.forEach(table => {
      table.classList.add("table-dark")
    })
    if (jumbotron) {
      jumbotron.classList.add("bg-dark")
      jumbotron.classList.remove("bg-light")
    }
  } else if (mode === "off") {
    elements.darkModeIcon.src = "sun.svg"
    // Dark Mode Improvement
    elements.searchBar.classList.remove("dark-mode-search")
    elements.formControls.forEach(input => input.classList.remove("bg-dark", "text-white"))
    // Remove the table-dark class for light mode
    var tables = document.querySelectorAll(".table-dark")
    tables.forEach(table => {
      table.classList.remove("table-dark")
    })
    if (jumbotron) {
      console.log(mode)
      jumbotron.classList.remove("bg-dark")
      jumbotron.classList.add("bg-light")
    }
  } else if (mode === "rainbow") {
    elements.darkModeIcon.src = "rainbow.svg"
  }
}

function toggleDarkMode() {
  var currentTime = Date.now()
  if (currentTime - lastToggleTime < 1000) {
    toggleCount++
  } else {
    toggleCount = 1
  }
  lastToggleTime = currentTime

  if (toggleCount >= 18) {
    localStorage.setItem("dark-mode", "rainbow")
    setMode("rainbow")
  } else if (localStorage.getItem("dark-mode") == "on") {
    localStorage.setItem("dark-mode", "off")
    setMode("off")
  } else {
    localStorage.setItem("dark-mode", "on")
    setMode("on")
  }
}

document.addEventListener("DOMContentLoaded", function () {
  getElements()

  var currentMode = localStorage.getItem("dark-mode")
  if (currentMode === "on" || currentMode === "off" || currentMode === "rainbow") {
    setMode(currentMode)
  } else if (window.matchMedia && window.matchMedia("(prefers-color-scheme: dark)").matches) {
    setMode("on")
  } else {
    setMode("off")
  }

  document.getElementById("dark-mode-toggle").addEventListener("click", function (event) {
    event.preventDefault()
    toggleDarkMode()
  })
})
