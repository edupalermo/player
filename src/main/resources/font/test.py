import keras_ocr
import matplotlib.pyplot as plt
import sys

# Check command-line argument
if len(sys.argv) < 2:
    print("Usage: python test.py path_to_image.png")
    sys.exit(1)

image_path = sys.argv[1]

# Load the pretrained pipeline
pipeline = keras_ocr.pipeline.Pipeline()

# Read the image
image = keras_ocr.tools.read(image_path)

# Run OCR
prediction_groups = pipeline.recognize([image])

# Print recognized text
for text, box in prediction_groups[0]:
    print(f'Detected: "{text}" at {box}')

# Draw annotations directly on the image
keras_ocr.tools.drawAnnotations(image=image, predictions=prediction_groups[0])

# Display image using matplotlib
plt.imshow(image)
plt.axis('off')
plt.show()
