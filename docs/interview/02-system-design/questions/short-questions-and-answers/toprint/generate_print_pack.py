from __future__ import annotations

import re
from dataclasses import dataclass
from pathlib import Path

from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER, TA_LEFT
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import mm
from reportlab.platypus import (
    BaseDocTemplate,
    Frame,
    KeepTogether,
    PageBreak,
    PageTemplate,
    Paragraph,
    Spacer,
    Table,
    TableStyle,
)
from reportlab.pdfbase.pdfmetrics import stringWidth


ROOT = Path(__file__).resolve().parent
SOURCE = ROOT / "system-design-morning-cheat-sheet.md"
MASTER_INDEX = ROOT / "index.md"
CHEAT_SHEET_PDF = ROOT / "system-design-morning-cheat-sheet.pdf"
TRACKER_PDF = ROOT / "system-design-learning-tracker.pdf"
TRACKER_MARKDOWN = ROOT / "system-design-learning-tracker.md"

NAVY = colors.HexColor("#16324F")
BLUE = colors.HexColor("#1F5D8F")
PALE_BLUE = colors.HexColor("#EAF2F8")
MID_GRAY = colors.HexColor("#65727E")
LIGHT_GRAY = colors.HexColor("#D9E0E6")
DARK = colors.HexColor("#17212B")


@dataclass(frozen=True)
class Question:
    question_id: str
    category: str
    prompt: str
    answer: str


@dataclass(frozen=True)
class Concept:
    concept_id: int
    category_number: int
    category: str
    name: str


def parse_source() -> list[Question]:
    category = ""
    question_id = ""
    prompt = ""
    answer_lines: list[str] = []
    questions: list[Question] = []

    def flush() -> None:
        nonlocal question_id, prompt, answer_lines
        if not question_id:
            return
        answer = " ".join(line.strip() for line in answer_lines if line.strip())
        questions.append(Question(question_id, category, prompt, answer))
        question_id = ""
        prompt = ""
        answer_lines = []

    for raw_line in SOURCE.read_text(encoding="utf-8").splitlines():
        if raw_line.startswith("## "):
            flush()
            category = re.sub(r"^##\s+", "", raw_line).strip()
        elif raw_line.startswith("### Q"):
            flush()
            match = re.match(r"^###\s+(Q\d{3})\s+-\s+(.+)$", raw_line)
            if not match:
                raise ValueError(f"Invalid question heading: {raw_line}")
            question_id, prompt = match.groups()
        elif question_id and raw_line.strip():
            answer_lines.append(raw_line)
    flush()

    expected_ids = [f"Q{index:03d}" for index in range(1, 133)]
    actual_ids = [question.question_id for question in questions]
    if actual_ids != expected_ids:
        raise ValueError(f"Expected sequential Q001-Q120, got {actual_ids[:3]}...{actual_ids[-3:]}")
    if any(not question.category or not question.answer for question in questions):
        raise ValueError("Every question needs a category and answer")
    return questions


def parse_master_index() -> list[Concept]:
    categories: list[tuple[int, str]] = []
    concepts: list[Concept] = []
    active_category_number = 0
    active_category = ""
    expected_concept_id = 1

    for raw_line in MASTER_INDEX.read_text(encoding="utf-8").splitlines():
        line = raw_line.strip()
        if not line:
            continue
        match = re.match(r"^(\d+)\.\s+(.+)$", line)
        if not match:
            raise ValueError(f"Invalid master-index line: {raw_line}")
        number = int(match.group(1))
        title = match.group(2).strip()

        if not categories or number != expected_concept_id:
            active_category_number = number
            active_category = title
            categories.append((number, title))
            continue

        concepts.append(Concept(number, active_category_number, active_category, title))
        expected_concept_id += 1

    if [number for number, _ in categories] != list(range(1, 17)):
        raise ValueError(f"Expected category numbers 1-16, got {[number for number, _ in categories]}")
    if [concept.concept_id for concept in concepts] != list(range(1, 381)):
        raise ValueError("Expected sequential concept IDs 1-380")
    return concepts


def cheat_sheet_styles():
    styles = getSampleStyleSheet()
    return {
        "title": ParagraphStyle(
            "PrintTitle",
            parent=styles["Title"],
            fontName="Helvetica-Bold",
            fontSize=15,
            leading=17,
            textColor=NAVY,
            alignment=TA_CENTER,
            spaceAfter=3 * mm,
        ),
        "intro": ParagraphStyle(
            "PrintIntro",
            parent=styles["BodyText"],
            fontName="Helvetica",
            fontSize=7.2,
            leading=8.6,
            textColor=MID_GRAY,
            alignment=TA_LEFT,
            spaceAfter=2 * mm,
        ),
        "category": ParagraphStyle(
            "Category",
            parent=styles["Heading2"],
            fontName="Helvetica-Bold",
            fontSize=9.2,
            leading=10.5,
            textColor=colors.white,
            backColor=NAVY,
            borderPadding=(2.3, 3.5, 2.3, 3.5),
            spaceBefore=1.4 * mm,
            spaceAfter=1.5 * mm,
        ),
        "question": ParagraphStyle(
            "Question",
            parent=styles["BodyText"],
            fontName="Helvetica-Bold",
            fontSize=7.35,
            leading=8.45,
            textColor=BLUE,
            spaceAfter=0.6 * mm,
        ),
        "answer": ParagraphStyle(
            "Answer",
            parent=styles["BodyText"],
            fontName="Helvetica",
            fontSize=7.05,
            leading=8.2,
            textColor=DARK,
            spaceAfter=1.45 * mm,
        ),
    }


def draw_cheat_sheet_page(canvas, doc) -> None:
    canvas.saveState()
    width, height = A4
    canvas.setStrokeColor(LIGHT_GRAY)
    canvas.setLineWidth(0.4)
    canvas.line(width / 2, 11 * mm, width / 2, height - 11 * mm)
    canvas.setFont("Helvetica", 6.5)
    canvas.setFillColor(MID_GRAY)
    canvas.drawString(9 * mm, 6.5 * mm, "System Design Morning Revision - 132 Questions")
    page_text = f"Page {doc.page}"
    canvas.drawString(width - 9 * mm - stringWidth(page_text, "Helvetica", 6.5), 6.5 * mm, page_text)
    canvas.restoreState()


def build_cheat_sheet(questions: list[Question]) -> None:
    width, height = A4
    margin_x = 8.5 * mm
    margin_top = 9 * mm
    margin_bottom = 11 * mm
    gap = 6 * mm
    column_width = (width - (2 * margin_x) - gap) / 2
    frame_height = height - margin_top - margin_bottom

    frames = [
        Frame(margin_x, margin_bottom, column_width, frame_height, leftPadding=0, rightPadding=0),
        Frame(margin_x + column_width + gap, margin_bottom, column_width, frame_height, leftPadding=0, rightPadding=0),
    ]
    doc = BaseDocTemplate(
        str(CHEAT_SHEET_PDF),
        pagesize=A4,
        leftMargin=margin_x,
        rightMargin=margin_x,
        topMargin=margin_top,
        bottomMargin=margin_bottom,
        title="System Design Morning Revision - 132 Questions",
        author="Instagram Backend Learning Project",
    )
    doc.addPageTemplates(PageTemplate(id="TwoColumn", frames=frames, onPage=draw_cheat_sheet_page))
    styles = cheat_sheet_styles()

    story = [
        Paragraph("SYSTEM DESIGN MORNING REVISION", styles["title"]),
        Paragraph(
            "Use the ID tracker to choose five questions each morning. Cover the answer, speak for 60-90 seconds, then check the decision, trade-off, and failure. Short answers are prompts for recall, not scripts to memorize.",
            styles["intro"],
        ),
    ]

    active_category = None
    for question in questions:
        if question.category != active_category:
            active_category = question.category
            story.append(Paragraph(active_category, styles["category"]))
        story.append(
            KeepTogether(
                [
                    Paragraph(f"<b>{question.question_id}</b>  {question.prompt}", styles["question"]),
                    Paragraph(question.answer, styles["answer"]),
                ]
            )
        )
    doc.build(story)


def tracker_styles():
    styles = getSampleStyleSheet()
    return {
        "title": ParagraphStyle(
            "TrackerTitle",
            parent=styles["Title"],
            fontName="Helvetica-Bold",
            fontSize=15,
            leading=17,
            textColor=NAVY,
            alignment=TA_CENTER,
            spaceAfter=2 * mm,
        ),
        "intro": ParagraphStyle(
            "TrackerIntro",
            parent=styles["BodyText"],
            fontName="Helvetica",
            fontSize=7.4,
            leading=9,
            textColor=MID_GRAY,
            alignment=TA_LEFT,
            spaceAfter=2.5 * mm,
        ),
        "cell": ParagraphStyle(
            "TrackerCell",
            parent=styles["BodyText"],
            fontName="Helvetica",
            fontSize=6.8,
            leading=7.7,
            textColor=DARK,
        ),
        "cell_bold": ParagraphStyle(
            "TrackerCellBold",
            parent=styles["BodyText"],
            fontName="Helvetica-Bold",
            fontSize=6.8,
            leading=7.7,
            textColor=DARK,
        ),
        "header": ParagraphStyle(
            "TrackerHeader",
            parent=styles["BodyText"],
            fontName="Helvetica-Bold",
            fontSize=6.8,
            leading=7.7,
            textColor=colors.white,
        ),
    }


def draw_tracker_page(canvas, doc) -> None:
    canvas.saveState()
    width, _ = A4
    canvas.setFont("Helvetica", 6.5)
    canvas.setFillColor(MID_GRAY)
    canvas.drawString(9 * mm, 6.5 * mm, "System Design Master Tracker - 380 Concepts")
    page_text = f"Page {doc.page}"
    canvas.drawString(width - 9 * mm - stringWidth(page_text, "Helvetica", 6.5), 6.5 * mm, page_text)
    canvas.restoreState()


def build_tracker(concepts: list[Concept]) -> None:
    width, _ = A4
    margin = 9 * mm
    doc = BaseDocTemplate(
        str(TRACKER_PDF),
        pagesize=A4,
        leftMargin=margin,
        rightMargin=margin,
        topMargin=9 * mm,
        bottomMargin=11 * mm,
        title="System Design Master Learning Tracker - 380 Concepts",
        author="Instagram Backend Learning Project",
    )
    frame = Frame(margin, 11 * mm, width - (2 * margin), A4[1] - 20 * mm, leftPadding=0, rightPadding=0)
    doc.addPageTemplates(PageTemplate(id="Tracker", frames=[frame], onPage=draw_tracker_page))
    styles = tracker_styles()

    story = [
        Paragraph("MASTER SYSTEM DESIGN TRACKER - 380 CONCEPTS", styles["title"]),
        Paragraph(
            "Master tracker for all 380 indexed concepts. Learned = core idea is correct. +1 and +7 = recalled after one and seven days. Explain = stated the decision, trade-off, and one failure without notes.",
            styles["intro"],
        ),
    ]

    rows = [[
        Paragraph("ID", styles["header"]),
        Paragraph("Question / concept", styles["header"]),
        Paragraph("Learned", styles["header"]),
        Paragraph("+1", styles["header"]),
        Paragraph("+7", styles["header"]),
        Paragraph("Explain", styles["header"]),
    ]]
    category_rows: list[int] = []
    active_category = None
    for concept in concepts:
        if concept.category != active_category:
            active_category = concept.category
            category_rows.append(len(rows))
            rows.append([
                Paragraph(f"{concept.category_number:02d}  {concept.category}", styles["header"]),
                "",
                "",
                "",
                "",
                "",
            ])
        rows.append([
            Paragraph(str(concept.concept_id), styles["cell_bold"]),
            Paragraph(concept.name, styles["cell"]),
            "[ ]",
            "[ ]",
            "[ ]",
            "[ ]",
        ])

    log_heading_row = len(rows)
    rows.append([Paragraph("MORNING REVIEW LOG", styles["header"]), "", "", "", "", ""])
    log_instruction_row = len(rows)
    rows.append([
        Paragraph(
            "Record the missed idea, date, concept IDs reviewed, and how many of five answers were correct before reading.",
            styles["cell"],
        ),
        "",
        "",
        "",
        "",
        "",
    ])
    log_columns_row = len(rows)
    rows.append([
        Paragraph("Missed idea / repair", styles["header"]),
        "",
        Paragraph("Date", styles["header"]),
        Paragraph("Concept IDs", styles["header"]),
        "",
        Paragraph("Score / 5", styles["header"]),
    ])
    log_blank_rows = list(range(len(rows), len(rows) + 8))
    rows.extend([["", "", "", "", "", ""] for _ in range(8)])

    table = Table(
        rows,
        colWidths=[12 * mm, 112 * mm, 17 * mm, 13 * mm, 13 * mm, 20 * mm],
        repeatRows=1,
        hAlign="CENTER",
    )
    table.setStyle(TableStyle([
        ("BACKGROUND", (0, 0), (-1, 0), NAVY),
        ("TEXTCOLOR", (0, 0), (-1, 0), colors.white),
        ("FONTNAME", (2, 1), (-1, -1), "Helvetica"),
        ("FONTSIZE", (2, 1), (-1, -1), 7),
        ("ALIGN", (2, 1), (-1, -1), "CENTER"),
        ("VALIGN", (0, 0), (-1, -1), "MIDDLE"),
        ("GRID", (0, 0), (-1, -1), 0.35, LIGHT_GRAY),
        ("ROWBACKGROUNDS", (0, 1), (-1, -1), [colors.white, PALE_BLUE]),
        ("TOPPADDING", (0, 0), (-1, -1), 2.2),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 2.2),
        ("LEFTPADDING", (0, 0), (-1, -1), 3),
        ("RIGHTPADDING", (0, 0), (-1, -1), 3),
    ]))
    for row_number in category_rows:
        table.setStyle(TableStyle([
            ("SPAN", (0, row_number), (-1, row_number)),
            ("BACKGROUND", (0, row_number), (-1, row_number), BLUE),
            ("TEXTCOLOR", (0, row_number), (-1, row_number), colors.white),
            ("TOPPADDING", (0, row_number), (-1, row_number), 2.5),
            ("BOTTOMPADDING", (0, row_number), (-1, row_number), 2.5),
        ]))
    table.setStyle(TableStyle([
        ("SPAN", (0, log_heading_row), (-1, log_heading_row)),
        ("BACKGROUND", (0, log_heading_row), (-1, log_heading_row), BLUE),
        ("TOPPADDING", (0, log_heading_row), (-1, log_heading_row), 2.5),
        ("BOTTOMPADDING", (0, log_heading_row), (-1, log_heading_row), 2.5),
        ("SPAN", (0, log_instruction_row), (-1, log_instruction_row)),
        ("BACKGROUND", (0, log_instruction_row), (-1, log_instruction_row), PALE_BLUE),
        ("ALIGN", (0, log_instruction_row), (-1, log_instruction_row), "LEFT"),
        ("SPAN", (0, log_columns_row), (1, log_columns_row)),
        ("SPAN", (3, log_columns_row), (4, log_columns_row)),
        ("BACKGROUND", (0, log_columns_row), (-1, log_columns_row), NAVY),
    ]))
    for row_number in log_blank_rows:
        table.setStyle(TableStyle([
            ("SPAN", (0, row_number), (1, row_number)),
            ("SPAN", (3, row_number), (4, row_number)),
            ("TOPPADDING", (0, row_number), (-1, row_number), 5),
            ("BOTTOMPADDING", (0, row_number), (-1, row_number), 5),
            ("ALIGN", (0, row_number), (1, row_number), "LEFT"),
        ]))
    story.append(table)
    doc.build(story)


def build_tracker_markdown(concepts: list[Concept]) -> None:
    lines = [
        "# System Design Learning Tracker",
        "",
        "This tracker follows `index.md` exactly. Use a date or checkmark only after an honest attempt. `Explain` requires the core idea, trade-off, and one failure without notes.",
        "",
        "| ID | Question / concept | Learned | Review +1 day | Review +7 days | Explain without notes |",
        "|---|---|---|---|---|---|",
    ]
    active_category = None
    for concept in concepts:
        if concept.category != active_category:
            active_category = concept.category
            lines.extend(["", f"## {concept.category_number:02d} {concept.category}", ""])
        lines.append(f"| {concept.concept_id} | {concept.name} |  |  |  |  |")
    TRACKER_MARKDOWN.write_text("\n".join(lines) + "\n", encoding="utf-8")


def main() -> None:
    questions = parse_source()
    concepts = parse_master_index()
    build_tracker_markdown(concepts)
    build_cheat_sheet(questions)
    build_tracker(concepts)
    print(f"Created {CHEAT_SHEET_PDF}")
    print(f"Created {TRACKER_PDF}")


if __name__ == "__main__":
    main()
