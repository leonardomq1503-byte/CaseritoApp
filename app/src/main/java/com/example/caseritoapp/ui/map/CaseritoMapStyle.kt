package com.example.caseritoapp.ui.map

object CaseritoMapStyle {

    // Estilo Clean / Minimalista Caserito:
    // - Oculta negocios comerciales genéricos para destacar solo los comercios de Caserito
    // - Parques en verde esmeralda suave (#178B63)
    // - Océano Pacífico (Costa Verde) en azul limpio (#CDE5F7)
    // - Avenidas principales en tonos cálidos y fondo despejado
    val JSON_STYLE = """
        [
          {
            "featureType": "poi.business",
            "stylers": [
              { "visibility": "off" }
            ]
          },
          {
            "featureType": "poi.medical",
            "stylers": [
              { "visibility": "off" }
            ]
          },
          {
            "featureType": "poi.park",
            "elementType": "geometry.fill",
            "stylers": [
              { "color": "#e8f5ed" }
            ]
          },
          {
            "featureType": "poi.park",
            "elementType": "labels.text.fill",
            "stylers": [
              { "color": "#178b63" }
            ]
          },
          {
            "featureType": "road",
            "elementType": "geometry.fill",
            "stylers": [
              { "color": "#ffffff" }
            ]
          },
          {
            "featureType": "road",
            "elementType": "geometry.stroke",
            "stylers": [
              { "color": "#ebebeb" }
            ]
          },
          {
            "featureType": "road.arterial",
            "elementType": "geometry.fill",
            "stylers": [
              { "color": "#fff0ee" }
            ]
          },
          {
            "featureType": "road.arterial",
            "elementType": "geometry.stroke",
            "stylers": [
              { "color": "#f8c4be" }
            ]
          },
          {
            "featureType": "road.highway",
            "elementType": "geometry.fill",
            "stylers": [
              { "color": "#ffd8d3" }
            ]
          },
          {
            "featureType": "road.highway",
            "elementType": "geometry.stroke",
            "stylers": [
              { "color": "#f04432" },
              { "weight": 0.5 }
            ]
          },
          {
            "featureType": "water",
            "elementType": "geometry.fill",
            "stylers": [
              { "color": "#cde5f7" }
            ]
          },
          {
            "featureType": "water",
            "elementType": "labels.text.fill",
            "stylers": [
              { "color": "#5b92ba" }
            ]
          },
          {
            "featureType": "landscape.man_made",
            "elementType": "geometry.fill",
            "stylers": [
              { "color": "#fafafa" }
            ]
          },
          {
            "featureType": "transit",
            "stylers": [
              { "visibility": "simplified" }
            ]
          }
        ]
    """.trimIndent()
}
