package org.assayflux.svgstudio

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import android.text.InputType
import java.util.Locale

data class SvgLayer(
    val id: Int,
    var name: String,
    var markup: String,
    var visible: Boolean = true,
    var opacity: Float = 1f
)

class MainActivity : Activity() {
    private val paper = Color.rgb(246, 244, 238)
    private val ink = Color.rgb(36, 67, 74)
    private val accent = Color.rgb(177, 139, 94)
    private lateinit var preview: WebView
    private lateinit var source: EditText
    private lateinit var layerList: LinearLayout
    private lateinit var status: TextView
    private lateinit var widthInput: EditText
    private lateinit var heightInput: EditText
    private lateinit var titleInput: EditText
    private lateinit var fillInput: EditText
    private lateinit var strokeInput: EditText
    private lateinit var strokeWidthInput: EditText
    private lateinit var gradientStart: EditText
    private lateinit var gradientEnd: EditText
    private lateinit var gradientCheck: CheckBox
    private lateinit var gradientDirection: Spinner
    private val layers = mutableListOf<SvgLayer>()
    private var nextId = 1
    private var selectedId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = ink
        window.navigationBarColor = ink
        buildInterface()
        addStarterArtwork()
        refreshAll()
    }

    private fun buildInterface() {
        val scroll = ScrollView(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(14), dp(16), dp(18))
            setBackgroundColor(paper)
        }
        scroll.addView(root)
        setContentView(scroll)

        root.addView(TextView(this).apply {
            text = "SVG STUDIO"
            textSize = 25f
            setTextColor(ink)
            typeface = Typeface.create("serif", Typeface.BOLD)
        })
        root.addView(TextView(this).apply {
            text = "Vector editor · layers · gradients · source"
            textSize = 12f
            setTextColor(Color.DKGRAY)
        })

        section(root, "CANVAS")
        val dims = row(root)
        widthInput = edit("Width", "600", 4)
        heightInput = edit("Height", "400", 4)
        dims.addView(widthInput, weightParams())
        dims.addView(heightInput, weightParams())
        titleInput = edit("Artwork title", "My SVG artwork", 1)
        root.addView(titleInput, fullParams())
        val canvasButtons = row(root)
        canvasButtons.addView(action("Apply canvas") {
            refreshAll()
            message("Canvas dimensions applied.")
        }, weightParams())
        canvasButtons.addView(action("Reset artwork") {
            AlertDialog.Builder(this)
                .setTitle("Reset artwork?")
                .setMessage("This replaces the current layer list with the starter design.")
                .setPositiveButton("Reset") { _, _ ->
                    layers.clear()
                    nextId = 1
                    addStarterArtwork()
                    refreshAll()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }, weightParams())

        section(root, "ADD VECTOR ELEMENTS")
        val add1 = row(root)
        add1.addView(action("+ Rectangle") { addRectangle() }, weightParams())
        add1.addView(action("+ Circle") { addCircle() }, weightParams())
        add1.addView(action("+ Ellipse") { addEllipse() }, weightParams())
        val add2 = row(root)
        add2.addView(action("+ Line") { addLine() }, weightParams())
        add2.addView(action("+ Text") { addTextLayer() }, weightParams())
        add2.addView(action("+ Star") { addStar() }, weightParams())
        val add3 = row(root)
        add3.addView(action("+ Triangle") { addTriangle() }, weightParams())
        add3.addView(action("+ Path") { addCustomPath() }, weightParams())

        section(root, "STYLE THE NEXT ELEMENT")
        fillInput = edit("Fill colour (#RRGGBB)", "#B18B5E", 1)
        strokeInput = edit("Stroke colour (#RRGGBB)", "#24434A", 1)
        strokeWidthInput = edit("Stroke width", "3", 2)
        root.addView(fillInput, fullParams())
        root.addView(strokeInput, fullParams())
        root.addView(strokeWidthInput, fullParams())
        gradientCheck = CheckBox(this).apply {
            text = "Use linear gradient fill"
            setTextColor(ink)
        }
        root.addView(gradientCheck)
        gradientStart = edit("Gradient start", "#B18B5E", 1)
        gradientEnd = edit("Gradient end", "#24434A", 1)
        root.addView(gradientStart, fullParams())
        root.addView(gradientEnd, fullParams())
        gradientDirection = Spinner(this).apply {
            adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item,
                listOf("Horizontal", "Vertical", "Diagonal"))
        }
        root.addView(gradientDirection, fullParams())
        root.addView(action("Add styled rectangle") { addRectangle() }, fullParams())

        section(root, "LAYERS")
        layerList = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(layerList, fullParams())
        val layerActions = row(root)
        layerActions.addView(action("Move up") { moveSelected(-1) }, weightParams())
        layerActions.addView(action("Move down") { moveSelected(1) }, weightParams())
        layerActions.addView(action("Edit selected") { editSelected() }, weightParams())
        layerActions.addView(action("Delete") { deleteSelected() }, weightParams())

        section(root, "LIVE PREVIEW")
        preview = WebView(this).apply {
            settings.javaScriptEnabled = false
            settings.allowFileAccess = false
            settings.domStorageEnabled = false
            webViewClient = WebViewClient()
            setBackgroundColor(Color.WHITE)
        }
        root.addView(preview, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(280)
        ).apply { topMargin = dp(6); bottomMargin = dp(8) })
        root.addView(action("Refresh preview") { refreshAll() }, fullParams())

        section(root, "SVG SOURCE CODE")
        source = EditText(this).apply {
            hint = "SVG/XML source"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            typeface = Typeface.MONOSPACE
            textSize = 11f
            minLines = 8
            maxLines = 16
            gravity = Gravity.TOP
            setTextColor(Color.rgb(35, 42, 44))
            setBackgroundColor(Color.WHITE)
            setPadding(dp(10), dp(10), dp(10), dp(10))
        }
        root.addView(source, fullParams())
        val sourceActions = row(root)
        sourceActions.addView(action("Apply source") {
            val value = source.text.toString()
            if (validSvg(value)) {
                preview.loadDataWithBaseURL(null, value, "image/svg+xml", "UTF-8", null)
                message("Custom SVG source previewed. Layer list remains unchanged until you rebuild elements.")
            } else message("Source does not look like valid SVG.")
        }, weightParams())
        sourceActions.addView(action("Copy source") { copySource() }, weightParams())
        sourceActions.addView(action("Export SVG") { exportSvg() }, weightParams())
        sourceActions.addView(action("Import SVG") { importSvg() }, weightParams())
        status = TextView(this).apply {
            text = "Ready · Offline vector editor"
            textSize = 11f
            setTextColor(Color.DKGRAY)
            setPadding(0, dp(8), 0, dp(4))
        }
        root.addView(status)
    }

    private fun addStarterArtwork() {
        layers += SvgLayer(nextId++, "Background", """<rect x="0" y="0" width="600" height="400" rx="22" fill="#F6F4EF"/>""")
        layers += SvgLayer(nextId++, "Main emblem", """<circle cx="300" cy="170" r="95" fill="#B18B5E"/><rect x="230" y="100" width="140" height="140" rx="24" fill="none" stroke="#24434A" stroke-width="10" transform="rotate(45 300 170)"/>""")
        layers += SvgLayer(nextId++, "Title", """<text x="300" y="330" text-anchor="middle" font-family="serif" font-size="28" fill="#24434A">SVG STUDIO</text>""")
        selectedId = layers.last().id
    }

    private fun addRectangle() {
        val x = 90; val y = 90
        addLayer("Rectangle", """<rect x="$x" y="$y" width="220" height="120" rx="12" fill="${fillValue()}" stroke="${strokeValue()}" stroke-width="${strokeWidth()}"/>""")
    }
    private fun addCircle() {
        addLayer("Circle", """<circle cx="300" cy="190" r="75" fill="${fillValue()}" stroke="${strokeValue()}" stroke-width="${strokeWidth()}"/>""")
    }
    private fun addEllipse() {
        addLayer("Ellipse", """<ellipse cx="300" cy="190" rx="120" ry="65" fill="${fillValue()}" stroke="${strokeValue()}" stroke-width="${strokeWidth()}"/>""")
    }
    private fun addLine() {
        addLayer("Line", """<line x1="90" y1="100" x2="500" y2="280" stroke="${strokeValue()}" stroke-width="${strokeWidth()}" stroke-linecap="round"/>""")
    }
    private fun addTextLayer() {
        val input = EditText(this).apply { setText("Your text"); setSingleLine() }
        AlertDialog.Builder(this).setTitle("Text element").setView(input)
            .setPositiveButton("Add") { _, _ ->
                val txt = esc(input.text.toString())
                addLayer("Text", """<text x="300" y="205" text-anchor="middle" font-family="sans-serif" font-size="38" fill="${fillValue()}">$txt</text>""")
            }.setNegativeButton("Cancel", null).show()
    }
    private fun addStar() {
        val points = (0 until 10).joinToString(" ") { i ->
            val angle = Math.PI * i / 5 - Math.PI / 2
            val radius = if (i % 2 == 0) 90.0 else 40.0
            String.format(Locale.US, "%.1f,%.1f", 300 + kotlin.math.cos(angle) * radius, 190 + kotlin.math.sin(angle) * radius)
        }
        addLayer("Star", """<polygon points="$points" fill="${fillValue()}" stroke="${strokeValue()}" stroke-width="${strokeWidth()}"/>""")
    }
    private fun addTriangle() {
        addLayer("Triangle", """<polygon points="300,75 430,290 170,290" fill="${fillValue()}" stroke="${strokeValue()}" stroke-width="${strokeWidth()}"/>""")
    }
    private fun addCustomPath() {
        val input = EditText(this).apply {
            setText("M 100 250 C 180 40 420 40 500 250")
            minLines = 2
        }
        AlertDialog.Builder(this).setTitle("SVG path data (d)").setMessage("Enter SVG path commands.")
            .setView(input).setPositiveButton("Add") { _, _ ->
                addLayer("Path", """<path d="${esc(input.text.toString())}" fill="none" stroke="${strokeValue()}" stroke-width="${strokeWidth()}" stroke-linecap="round"/>""")
            }.setNegativeButton("Cancel", null).show()
    }

    private fun addLayer(name: String, rawMarkup: String) {
        val markup = if (gradientCheck.isChecked && !rawMarkup.contains("fill=\"none\"")) {
            rawMarkup.replace(Regex("""fill="[^"]*"""")) { """fill="url(#studioGradient)"""" }
        } else rawMarkup
        layers += SvgLayer(nextId++, name, markup)
        selectedId = layers.last().id
        refreshAll()
        message("$name added.")
    }

    private fun refreshAll() {
        val svg = buildSvg()
        if (::source.isInitialized) source.setText(svg)
        if (::preview.isInitialized) preview.loadDataWithBaseURL(null, svg, "image/svg+xml", "UTF-8", null)
        if (::layerList.isInitialized) rebuildLayerList()
    }

    private fun buildSvg(): String {
        val w = widthInput.text.toString().toIntOrNull()?.coerceIn(1, 10000) ?: 600
        val h = heightInput.text.toString().toIntOrNull()?.coerceIn(1, 10000) ?: 400
        val defs = if (layers.any { it.markup.contains("url(#studioGradient)") }) {
            val direction = when (gradientDirection.selectedItemPosition) {
                1 -> """x1="0%" y1="0%" x2="0%" y2="100%""""
                2 -> """x1="0%" y1="0%" x2="100%" y2="100%""""
                else -> """x1="0%" y1="0%" x2="100%" y2="0%""""
            }
            """<defs><linearGradient id="studioGradient" $direction><stop offset="0%" stop-color="${safeColor(gradientStart.text.toString(), "#B18B5E")}"/><stop offset="100%" stop-color="${safeColor(gradientEnd.text.toString(), "#24434A")}"/></linearGradient></defs>"""
        } else ""
        val body = layers.filter { it.visible }.joinToString("\n") { layer ->
            """<g id="layer-${layer.id}" opacity="${layer.opacity.coerceIn(0f, 1f)}">${layer.markup}</g>"""
        }
        val label = esc(titleInput.text.toString())
        return """<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" width="$w" height="$h" viewBox="0 0 $w $h" role="img" aria-label="$label">
<title>$label</title>
$defs
$body
</svg>"""
    }

    private fun rebuildLayerList() {
        layerList.removeAllViews()
        layers.forEachIndexed { index, layer ->
            val line = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(8), dp(4), dp(4), dp(4))
                setBackgroundColor(if (layer.id == selectedId) Color.rgb(228, 220, 204) else Color.WHITE)
            }
            val eye = CheckBox(this).apply {
                isChecked = layer.visible
                setOnCheckedChangeListener { _, checked -> layer.visible = checked; refreshAll() }
            }
            line.addView(eye)
            val label = TextView(this).apply {
                text = "${index + 1}. ${layer.name}"
                textSize = 13f
                setTextColor(ink)
                setPadding(dp(5), 0, dp(5), 0)
                setOnClickListener { selectedId = layer.id; rebuildLayerList() }
            }
            line.addView(label, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            val opacity = Button(this).apply {
                text = "Opacity"
                textSize = 10f
                setOnClickListener { opacityDialog(layer) }
            }
            line.addView(opacity)
            line.setOnClickListener { selectedId = layer.id; rebuildLayerList() }
            layerList.addView(line, fullParams())
        }
        if (layers.isEmpty()) layerList.addView(TextView(this).apply { text = "No layers yet"; setTextColor(Color.GRAY) })
    }

    private fun moveSelected(delta: Int) {
        val i = layers.indexOfFirst { it.id == selectedId }
        val j = i + delta
        if (i < 0 || j !in layers.indices) return
        val item = layers.removeAt(i); layers.add(j, item)
        refreshAll()
    }
    private fun editSelected() {
        val layer = layers.firstOrNull { it.id == selectedId } ?: run { message("Select a layer first."); return }
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(24, 8, 24, 0) }
        val name = EditText(this).apply { setText(layer.name); hint = "Layer name" }
        val markup = EditText(this).apply {
            setText(layer.markup); hint = "SVG element markup"; minLines = 5
            typeface = Typeface.MONOSPACE; textSize = 11f
        }
        box.addView(name); box.addView(markup)
        AlertDialog.Builder(this).setTitle("Edit layer").setView(box)
            .setPositiveButton("Save") { _, _ ->
                layer.name = name.text.toString().ifBlank { "Layer" }
                layer.markup = markup.text.toString()
                refreshAll()
            }.setNegativeButton("Cancel", null).show()
    }
    private fun deleteSelected() {
        val index = layers.indexOfFirst { it.id == selectedId }
        if (index < 0) { message("Select a layer first."); return }
        layers.removeAt(index)
        selectedId = layers.lastOrNull()?.id
        refreshAll()
    }
    private fun opacityDialog(layer: SvgLayer) {
        val seek = SeekBar(this).apply { max = 100; progress = (layer.opacity * 100).toInt() }
        AlertDialog.Builder(this).setTitle("Layer opacity")
            .setView(seek)
            .setPositiveButton("Apply") { _, _ -> layer.opacity = seek.progress / 100f; refreshAll() }
            .setNegativeButton("Cancel", null).show()
    }

    private fun copySource() {
        val cm = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("SVG source", source.text.toString()))
        message("SVG source copied.")
    }
    private fun exportSvg() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/svg+xml"
            putExtra(Intent.EXTRA_TITLE, "svg-studio-artwork.svg")
        }
        startActivityForResult(intent, 42)
    }
    private fun importSvg() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        startActivityForResult(intent, 43)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) return
        val uri = data?.data ?: return
        try {
            when (requestCode) {
                42 -> {
                    contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(source.text.toString()) }
                    message("SVG exported.")
                }
                43 -> {
                    val text = contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: ""
                    if (validSvg(text)) {
                        source.setText(text)
                        preview.loadDataWithBaseURL(null, text, "image/svg+xml", "UTF-8", null)
                        message("SVG imported into source editor. Use Apply source to preview again after edits.")
                    } else message("The selected file is not a recognizable SVG document.")
                }
            }
        } catch (e: Exception) { message("File operation failed: ${e.message ?: "unknown error"}") }
    }

    private fun validSvg(value: String) = value.trim().startsWith("<svg", true) ||
        value.trim().startsWith("<?xml", true) && value.contains("<svg", true)
    private fun fillValue() = if (gradientCheck.isChecked) "url(#studioGradient)" else safeColor(fillInput.text.toString(), "#B18B5E")
    private fun strokeValue() = safeColor(strokeInput.text.toString(), "#24434A")
    private fun strokeWidth() = strokeWidthInput.text.toString().toFloatOrNull()?.coerceIn(0f, 100f) ?: 3f
    private fun safeColor(value: String, fallback: String): String =
        if (Regex("^#(?:[0-9a-fA-F]{3}|[0-9a-fA-F]{6}|[0-9a-fA-F]{8})$").matches(value.trim())) value.trim() else fallback
    private fun esc(s: String) = s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;")
    private fun message(text: String) { if (::status.isInitialized) status.text = text }
    private fun section(parent: LinearLayout, text: String) {
        parent.addView(TextView(this).apply {
            this.text = text; textSize = 12f; setTextColor(ink)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, dp(14), 0, dp(5))
        })
    }
    private fun row(parent: LinearLayout) = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        parent.addView(this, fullParams())
    }
    private fun edit(hintText: String, value: String, lines: Int): EditText = EditText(this).apply {
        hint = hintText; setText(value); textSize = 13f; setTextColor(Color.BLACK)
        if (lines == 1) setSingleLine() else minLines = lines
        setPadding(dp(8), dp(4), dp(8), dp(4))
    }
    private fun action(label: String, callback: () -> Unit) = Button(this).apply {
        text = label; textSize = 11f; isAllCaps = false
        setTextColor(ink)
        setOnClickListener { callback() }
    }
    private fun fullParams() = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
        bottomMargin = dp(4)
    }
    private fun weightParams() = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
        setMargins(dp(2), dp(2), dp(2), dp(4))
    }
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
